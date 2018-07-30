/*
 * Copyright © 2018 Edwin Njeru (mailnjeru@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.mali.fasaha.utils;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import java.io.PrintWriter;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Executes code and wraps functions, sending any errors to a {@code Consumer<Throwable>} error handler.
 * 
 *
 * @author edwin_njeru
 * @version $Id: $Id
 */
public abstract class Errors implements Consumer<Throwable> {
    private static final Handling suppress = createHandling(Consumers.doNothing());
    private static final Rethrowing rethrow = createRethrowing(Errors::rethrowErrorAndWrapOthersAsRuntime);
    private static Handling log;
    private static Handling dialog;
    protected final Consumer<Throwable> handler;

    /**
     * <p>Constructor for Errors.</p>
     *
     * @param error a {@link java.util.function.Consumer} object.
     */
    protected Errors(Consumer<Throwable> error) {
        this.handler = error;
    }

    /**
     * Package-private for testing - resets all of the static member variables.
     */
    static void resetForTesting() {
        log = null;
        dialog = null;
    }

    /**
     * Creates an Errors.Handling which passes any exceptions it receives to the given handler.
     * <p>
     * The handler is free to throw a RuntimeException if it wants to. If it always throws a RuntimeException, then you should instead create an Errors.Rethrowing using {@link #createRethrowing}.
     *
     * @param handler a {@link java.util.function.Consumer} object.
     * @return a {@link org.mali.fasaha.utils.Errors.Handling} object.
     */
    public static Handling createHandling(Consumer<Throwable> handler) {

        return new Handling(handler);
    }

    /**
     * Creates an Errors.Rethrowing which transforms any exceptions it receives into a RuntimeException as specified by the given function, and then throws that RuntimeException.
     * <p>
     * If that function happens to throw an unchecked error itself, that'll work just fine too.
     *
     * @param transform a {@link java.util.function.Function} object.
     * @return a {@link org.mali.fasaha.utils.Errors.Rethrowing} object.
     */
    public static Rethrowing createRethrowing(Function<Throwable, RuntimeException> transform) {
        return new Rethrowing(transform);
    }

    /**
     * Suppresses errors entirely.
     *
     * @return a {@link org.mali.fasaha.utils.Errors.Handling} object.
     */
    public static Handling suppress() {
        return suppress;
    }

    /**
     * Rethrows any exceptions as runtime exceptions.
     *
     * @return a {@link org.mali.fasaha.utils.Errors.Rethrowing} object.
     */
    public static Rethrowing rethrow() {
        return rethrow;
    }

    private static RuntimeException rethrowErrorAndWrapOthersAsRuntime(Throwable e) {
        if (e instanceof Error) {
            throw (Error) e;
        } else {
            return Errors.asRuntime(e);
        }
    }

    /**
     * Logs any exceptions.
     * <br>
     * By default, log() calls {@code Throwable.printStackTrace()}.
     * To modify this behavior in your application, call
     * {@code Plugins.set(Errors.Plugins.Log.class, error -> myCustomLog(error));}
     *
     * @see org.mali.fasaha.utils.Plugins
     * @see Plugins.OnErrorThrowAssertion
     * @return a {@link org.mali.fasaha.utils.Errors.Handling} object.
     */
    @SuppressFBWarnings(value = "LI_LAZY_INIT_STATIC", justification = "This race condition is fine, as explained in the comment below.")
    public static Handling log() {
        if (log == null) {

            //There is an acceptable race condition here - log might get set multiple times.
            //This would happen if multiple threads called log() at the same time
            //during initialization, and this is likely to actually happen in practice.
            //
            //Because Plugins guarantees that its methods will have the exact same
            //return value for the duration of the library's runtime existence, the only
            //adverse symptom of this race condition is that there will temporarily be
            //multiple instances of Errors which are wrapping the same Consumer<Throwable>.
            //
            //It is important for this method to be fast, so it's better to accept
            //that suppress() might return different Errors instances which are wrapping
            //the same actual Consumer<Throwable>, rather than to incur the cost of some
            //type of synchronization.

            log = createHandling(org.mali.fasaha.utils.Plugins.get(Plugins.Log.class, Errors.Plugins::defaultLog));
        }
        return log;
    }

    /**
     * Opens a dialog to notify the user of any exceptions.  It should be used in cases where an error is too severe to be silently logged.
     * <p>
     * By default, dialog() opens a JOptionPane. To modify this behavior in your application, call {@code Plugins.set(Errors.Plugins.Dialog.class, error -> openMyDialog(error));}
     * <p>
     * For a non-interactive console application, a good implementation of would probably print the error and call System.exit().
     *
     * @see org.mali.fasaha.utils.Plugins
     * @see Plugins.OnErrorThrowAssertion
     * @return a {@link org.mali.fasaha.utils.Errors.Handling} object.
     */
    @SuppressFBWarnings(value = "LI_LAZY_INIT_STATIC", justification = "This race condition is fine, as explained in the comment below.")
    public static Handling dialog() {
        if (dialog == null) {
            // There is an acceptable race condition here.  See Errors.log() for details.
            dialog = createHandling(org.mali.fasaha.utils.Plugins.get(Plugins.Dialog.class, Errors.Plugins::defaultDialog));
        }
        return dialog;
    }

    /**
     * Casts or wraps the given exception to be a RuntimeException.
     *
     * @param e a {@link java.lang.Throwable} object.
     * @return a {@link java.lang.RuntimeException} object.
     */
    public static RuntimeException asRuntime(Throwable e) {
        if (e instanceof RuntimeException) {
            return (RuntimeException) e;
        } else {
            return new WrappedAsRuntimeException(e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * Passes the given error to this Errors.
     */
    @Override
    public void accept(Throwable error) {
        handler.accept(error);
    }

    /**
     * Converts this {@code Consumer<Throwable>} to a {@code Consumer<Optional<Throwable>>}.
     *
     * @return a {@link java.util.function.Consumer} object.
     */
    public Consumer<Optional<Throwable>> asTerminal() {
        return errorOpt -> {
            if (errorOpt.isPresent()) {
                accept(errorOpt.get());
            }
        };
    }

    /**
     * Attempts to run the given runnable.
     *
     * @param runnable a {@link org.mali.fasaha.utils.Throwing.Runnable} object.
     */
    public void run(Throwing.Runnable runnable) {
        wrap(runnable).run();
    }

    /**
     * Returns a Runnable whose exceptions are handled by this Errors.
     *
     * @param runnable a {@link org.mali.fasaha.utils.Throwing.Runnable} object.
     * @return a {@link java.lang.Runnable} object.
     */
    public Runnable wrap(Throwing.Runnable runnable) {
        return () -> {
            try {
                runnable.run();
            } catch (Throwable e) {
                handler.accept(e);
            }
        };
    }

    /**
     * Returns a Consumer whose exceptions are handled by this Errors.
     *
     * @param consumer a {@link org.mali.fasaha.utils.Throwing.Consumer} object.
     * @return a {@link java.util.function.Consumer} object.
     * @param <T> a T object.
     */
    public <T> Consumer<T> wrap(Throwing.Consumer<T> consumer) {
        return val -> {
            try {
                consumer.accept(val);
            } catch (Throwable e) {
                handler.accept(e);
            }
        };
    }

    /**
     * Returns a Consumer whose exceptions are handled by this Errors.
     *
     * @param consumer a {@link org.mali.fasaha.utils.Throwing.Consumer} object.
     * @return a {@link java.util.function.Consumer} object.
     * @param <T> a T object.
     */
    public <T> Consumer<T> wrap(Throwing.IntConsumer consumer) {
        return val -> {
            try {
                consumer.accept((Integer) val);
            } catch (Throwable e) {
                handler.accept(e);
            }
        };
    }

    /**
     * Namespace for the plugins which Errors supports.
     */
    public interface Plugins {
        /**
         * Default behavior of {@link Errors#log} is @{code Throwable.printStackTrace()}.
         */
        static void defaultLog(Throwable error) {
            error.printStackTrace();
        }

        /**
         * Default behavior of {@link Errors#dialog} is @{code JOptionPane.showMessageDialog} without a parent.
         */
        static void defaultDialog(Throwable error) {
            SwingUtilities.invokeLater(() -> {
                error.printStackTrace();
                String title = error.getClass()
                                    .getSimpleName();
                JOptionPane.showMessageDialog(null, error.getMessage() + "\n\n" + StringPrinter.buildString(printer -> {
                    PrintWriter writer = printer.toPrintWriter();
                    error.printStackTrace(writer);
                    writer.close();
                }), title, JOptionPane.ERROR_MESSAGE);
            });
        }

        /**
         * Plugin interface for {@link Errors#log}.
         */
        public interface Log extends Consumer<Throwable> {
        }

        /**
         * Plugin interface for {@link Errors#dialog}.
         */
        public interface Dialog extends Consumer<Throwable> {
        }

        /**
         * An implementation of all of the {@link Errors} plugins which throws an AssertionError on any exception.  This can be helpful for JUnit tests.
         * <p>
         * To enable this in your application, you can either:
         * <ul>
         * <li>Execute this code at the very beginning of your application:<pre>
         * Plugins.set(Errors.Plugins.Log.class, new OnErrorThrowAssertion());
         * Plugins.set(Errors.Plugins.Dialog.class, new OnErrorThrowAssertion());
         * </pre></li>
         * <li>Set these system properties:<pre>
         * durian.plugins.com.diffplug.common.base.Errors.Plugins.Log=com.diffplug.common.base.Errors$Plugins$OnErrorThrowAssertion
         * durian.plugins.com.diffplug.common.base.Errors.Plugins.Dialog=com.diffplug.common.base.Errors$Plugins$OnErrorThrowAssertion
         * </pre></li>
         * </ul>
         *
         * @see org.mali.fasaha.utils.Plugins
         */
        public static class OnErrorThrowAssertion implements Log, Dialog {
            @Override
            public void accept(Throwable error) {
                throw new AssertionError(error);
            }
        }
    }

    /**
     * An {@link Errors} which is free to rethrow the exception, but it might not.
     * <p>
     * If we want to wrap a method with a return value, since the handler might not
     * throw an exception, we need a default value to return.
     */
    public static class Handling extends Errors {

        protected Handling(Consumer<Throwable> error) {
            super(error);
        }

        /**
         * Attempts to call {@code supplier} and returns {@code onFailure} if an exception is thrown.
         */
        public <T> T getWithDefault(Throwing.Supplier<T> supplier, T onFailure) {
            return wrapWithDefault(supplier, onFailure).get();
        }

        /**
         * Returns a Supplier which wraps {@code supplier} and returns {@code onFailure} if an exception is thrown.
         */
        public <T> Supplier<T> wrapWithDefault(Throwing.Supplier<T> supplier, T onFailure) {
            return () -> {
                try {
                    return supplier.get();
                } catch (Throwable e) {
                    handler.accept(e);
                    return onFailure;
                }
            };
        }

        /**
         * Returns a Function which wraps {@code function} and returns {@code onFailure} if an exception is thrown.
         * <p>
         * If you are getting an error about {@code the method wrapWithDefault is ambiguous}, use {@link #wrapFunctionWithDefault(Throwing.Function, Object)}  or {@link
         * #wrapPredicateWithDefault(Throwing.Predicate, boolean)}
         */
        public <T, R> Function<T, R> wrapWithDefault(Throwing.Function<T, R> function, R onFailure) {
            return wrapFunctionWithDefault(function, onFailure);
        }

        /**
         * Returns a Predicate which wraps {@code predicate} and returns {@code onFailure} if an exception is thrown.
         * <p>
         * If you are getting an error about {@code the method wrapWithDefault is ambiguous}, use {@link #wrapFunctionWithDefault(Throwing.Function, Object)}  or {@link
         * #wrapPredicateWithDefault(Throwing.Predicate, boolean)}
         */
        public <T> Predicate<T> wrapWithDefault(Throwing.Predicate<T> predicate, boolean onFailure) {
            return wrapPredicateWithDefault(predicate, onFailure);
        }

        /**
         * Returns a Function which wraps {@code function} and returns {@code onFailure} if an exception is thrown.
         */
        public <T, R> Function<T, R> wrapFunctionWithDefault(Throwing.Function<T, R> function, R onFailure) {
            return input -> {
                try {
                    return function.apply(input);
                } catch (Throwable e) {
                    handler.accept(e);
                    return onFailure;
                }
            };
        }

        /**
         * Returns a Predicate which wraps {@code predicate} and returns {@code onFailure} if an exception is thrown.
         */
        public <T> Predicate<T> wrapPredicateWithDefault(Throwing.Predicate<T> predicate, boolean onFailure) {
            return input -> {
                try {
                    return predicate.test(input);
                } catch (Throwable e) {
                    handler.accept(e);
                    return onFailure;
                }
            };
        }
    }

    /**
     * An {@link Errors} which is guaranteed to always throw a RuntimeException.
     * <p>
     * If we want to wrap a method with a return value, it's pointless to specify a default value because if the wrapped method fails, a RuntimeException is guaranteed to throw.
     */
    public static class Rethrowing extends Errors {
        private final Function<Throwable, RuntimeException> transform;

        protected Rethrowing(Function<Throwable, RuntimeException> transform) {
            super(error -> {
                throw transform.apply(error);
            });
            this.transform = transform;
        }

        /**
         * Attempts to call {@code supplier} and rethrows any exceptions as unchecked exceptions.
         */
        public <T> T get(Throwing.Supplier<T> supplier) {
            return wrap(supplier).get();
        }

        /**
         * Returns a Supplier which wraps {@code supplier} and rethrows any exceptions as unchecked exceptions.
         */
        public <T> Supplier<T> wrap(Throwing.Supplier<T> supplier) {
            return () -> {
                try {
                    return supplier.get();
                } catch (Throwable e) {
                    throw transform.apply(e);
                }
            };
        }

        /**
         * Returns a Function which wraps {@code function} and rethrows any exceptions as unchecked exceptions.
         * <p>
         * If you are getting an error about {@code the method wrap is ambiguous}, use {@link #wrapFunction(Throwing.Function)} or {@link #wrapPredicate(Throwing.Predicate)}.
         */
        public <T, R> Function<T, R> wrap(Throwing.Function<T, R> function) {
            return wrapFunction(function);
        }

        /**
         * Returns a Predicate which wraps {@code predicate} and rethrows any exceptions as unchecked exceptions.
         * <p>
         * If you are getting an error about {@code the method wrap is ambiguous},
         * use {@link #wrapFunction(Throwing.Function)} or {@link #wrapPredicate(Throwing.Predicate)}.
         */
        public <T> Predicate<T> wrap(Throwing.Predicate<T> predicate) {
            return wrapPredicate(predicate);
        }

        /**
         * Returns a Function which wraps {@code function} and rethrows any exceptions as unchecked exceptions.
         */
        public <T, R> Function<T, R> wrapFunction(Throwing.Function<T, R> function) {
            return arg -> {
                try {
                    return function.apply(arg);
                } catch (Throwable e) {
                    throw transform.apply(e);
                }
            };
        }

        /**
         * Returns a Predicate which wraps {@code predicate} and rethrows any exceptions as unchecked exceptions.
         */
        public <T> Predicate<T> wrapPredicate(Throwing.Predicate<T> predicate) {
            return arg -> {
                try {
                    return predicate.test(arg);
                } catch (Throwable e) {
                    throw transform.apply(e); // 1 855 548 2505
                }
            };
        }
    }

    /**
     * A RuntimeException specifically for the purpose of wrapping non-runtime Throwables as RuntimeExceptions.
     */
    public static class WrappedAsRuntimeException extends RuntimeException {
        private static final long serialVersionUID = -912202209702586994L;

        public WrappedAsRuntimeException(Throwable e) {
            super(e);
        }
    }
}

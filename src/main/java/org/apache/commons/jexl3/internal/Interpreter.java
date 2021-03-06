/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
//CSOFF: FileLength
package org.apache.commons.jexl3.internal;

import java.util.Iterator;
import java.util.concurrent.Callable;

import org.apache.commons.jexl3.JexlArithmetic;
import org.apache.commons.jexl3.JexlContext;
import org.apache.commons.jexl3.JexlEngine;
import org.apache.commons.jexl3.JexlException;
import org.apache.commons.jexl3.JexlOperator;
import org.apache.commons.jexl3.JexlOptions;
import org.apache.commons.jexl3.JexlScript;
import org.apache.commons.jexl3.JxltEngine;

import org.apache.commons.jexl3.introspection.JexlMethod;
import org.apache.commons.jexl3.introspection.JexlPropertyGet;

import org.apache.commons.jexl3.parser.ASTAddNode;
import org.apache.commons.jexl3.parser.ASTAndNode;
import org.apache.commons.jexl3.parser.ASTAnnotatedStatement;
import org.apache.commons.jexl3.parser.ASTAnnotation;
import org.apache.commons.jexl3.parser.ASTArguments;
import org.apache.commons.jexl3.parser.ASTArrayAccess;
import org.apache.commons.jexl3.parser.ASTArrayLiteral;
import org.apache.commons.jexl3.parser.ASTAssignment;
import org.apache.commons.jexl3.parser.ASTBitwiseAndNode;
import org.apache.commons.jexl3.parser.ASTBitwiseComplNode;
import org.apache.commons.jexl3.parser.ASTBitwiseOrNode;
import org.apache.commons.jexl3.parser.ASTBitwiseXorNode;
import org.apache.commons.jexl3.parser.ASTBlock;
import org.apache.commons.jexl3.parser.ASTBreak;
import org.apache.commons.jexl3.parser.ASTConstructorNode;
import org.apache.commons.jexl3.parser.ASTContinue;
import org.apache.commons.jexl3.parser.ASTDivNode;
import org.apache.commons.jexl3.parser.ASTDoWhileStatement;
import org.apache.commons.jexl3.parser.ASTEQNode;
import org.apache.commons.jexl3.parser.ASTERNode;
import org.apache.commons.jexl3.parser.ASTEWNode;
import org.apache.commons.jexl3.parser.ASTEmptyFunction;
import org.apache.commons.jexl3.parser.ASTExtendedLiteral;
import org.apache.commons.jexl3.parser.ASTFalseNode;
import org.apache.commons.jexl3.parser.ASTForeachStatement;
import org.apache.commons.jexl3.parser.ASTFunctionNode;
import org.apache.commons.jexl3.parser.ASTGENode;
import org.apache.commons.jexl3.parser.ASTGTNode;
import org.apache.commons.jexl3.parser.ASTIdentifier;
import org.apache.commons.jexl3.parser.ASTIdentifierAccess;
import org.apache.commons.jexl3.parser.ASTIdentifierAccessJxlt;
import org.apache.commons.jexl3.parser.ASTIfStatement;
import org.apache.commons.jexl3.parser.ASTJexlLambda;
import org.apache.commons.jexl3.parser.ASTJexlScript;
import org.apache.commons.jexl3.parser.ASTJxltLiteral;
import org.apache.commons.jexl3.parser.ASTLENode;
import org.apache.commons.jexl3.parser.ASTLTNode;
import org.apache.commons.jexl3.parser.ASTMapEntry;
import org.apache.commons.jexl3.parser.ASTMapLiteral;
import org.apache.commons.jexl3.parser.ASTMethodNode;
import org.apache.commons.jexl3.parser.ASTModNode;
import org.apache.commons.jexl3.parser.ASTMulNode;
import org.apache.commons.jexl3.parser.ASTNENode;
import org.apache.commons.jexl3.parser.ASTNEWNode;
import org.apache.commons.jexl3.parser.ASTNRNode;
import org.apache.commons.jexl3.parser.ASTNSWNode;
import org.apache.commons.jexl3.parser.ASTNotNode;
import org.apache.commons.jexl3.parser.ASTNullLiteral;
import org.apache.commons.jexl3.parser.ASTNullpNode;
import org.apache.commons.jexl3.parser.ASTNumberLiteral;
import org.apache.commons.jexl3.parser.ASTOrNode;
import org.apache.commons.jexl3.parser.ASTRangeNode;
import org.apache.commons.jexl3.parser.ASTReference;
import org.apache.commons.jexl3.parser.ASTReferenceExpression;
import org.apache.commons.jexl3.parser.ASTRegexLiteral;
import org.apache.commons.jexl3.parser.ASTReturnStatement;
import org.apache.commons.jexl3.parser.ASTSWNode;
import org.apache.commons.jexl3.parser.ASTSetAddNode;
import org.apache.commons.jexl3.parser.ASTSetAndNode;
import org.apache.commons.jexl3.parser.ASTSetDivNode;
import org.apache.commons.jexl3.parser.ASTSetLiteral;
import org.apache.commons.jexl3.parser.ASTSetModNode;
import org.apache.commons.jexl3.parser.ASTSetMultNode;
import org.apache.commons.jexl3.parser.ASTSetOrNode;
import org.apache.commons.jexl3.parser.ASTSetSubNode;
import org.apache.commons.jexl3.parser.ASTSetXorNode;
import org.apache.commons.jexl3.parser.ASTSizeFunction;
import org.apache.commons.jexl3.parser.ASTStringLiteral;
import org.apache.commons.jexl3.parser.ASTSubNode;
import org.apache.commons.jexl3.parser.ASTTernaryNode;
import org.apache.commons.jexl3.parser.ASTTrueNode;
import org.apache.commons.jexl3.parser.ASTUnaryMinusNode;
import org.apache.commons.jexl3.parser.ASTUnaryPlusNode;
import org.apache.commons.jexl3.parser.ASTVar;
import org.apache.commons.jexl3.parser.ASTWhileStatement;
import org.apache.commons.jexl3.parser.JexlNode;
import org.apache.commons.jexl3.parser.Node;

/**
 * An interpreter of JEXL syntax.
 *
 * @since 2.0
 */
public class Interpreter extends InterpreterBase {
    /** Frame height. */
    protected int fp = 0;
    /** Symbol values. */
    protected final Frame frame;
    /** Block micro-frames. */
    protected LexicalFrame block = null;

    /**
     * The thread local interpreter.
     */
    protected static final java.lang.ThreadLocal<Interpreter> INTER =
                       new java.lang.ThreadLocal<Interpreter>();

    /**
     * Creates an interpreter.
     * @param engine   the engine creating this interpreter
     * @param aContext the evaluation context, global variables, methods and functions
     * @param opts     the evaluation options, flags modifying evaluation behavior
     * @param eFrame   the evaluation frame, arguments and local variables
     */
    protected Interpreter(Engine engine, JexlOptions opts, JexlContext aContext, Frame eFrame) {
        super(engine, opts, aContext);
        this.frame = eFrame;
    }

    /**
     * Copy constructor.
     * @param ii  the interpreter to copy
     * @param jexla the arithmetic instance to use (or null)
     */
    protected Interpreter(Interpreter ii, JexlArithmetic jexla) {
        super(ii, jexla);
        frame = ii.frame;
        block = ii.block != null? new LexicalFrame(ii.block) : null;
    }

    /**
     * Swaps the current thread local interpreter.
     * @param inter the interpreter or null
     * @return the previous thread local interpreter
     */
    protected Interpreter putThreadInterpreter(Interpreter inter) {
        Interpreter pinter = INTER.get();
        INTER.set(inter);
        return pinter;
    }

    /**
     * Interpret the given script/expression.
     * <p>
     * If the underlying JEXL engine is silent, errors will be logged through
     * its logger as warning.
     * @param node the script or expression to interpret.
     * @return the result of the interpretation.
     * @throws JexlException if any error occurs during interpretation.
     */
    public Object interpret(JexlNode node) {
        JexlContext.ThreadLocal tcontext = null;
        JexlEngine tjexl = null;
        Interpreter tinter = null;
        try {
            tinter = putThreadInterpreter(this);
            if (tinter != null) {
                fp = tinter.fp + 1;
            }
            if (context instanceof JexlContext.ThreadLocal) {
                tcontext = jexl.putThreadLocal((JexlContext.ThreadLocal) context);
            }
            tjexl = jexl.putThreadEngine(jexl);
            if (fp > jexl.stackOverflow) {
                throw new JexlException.StackOverflow(node.jexlInfo(), "jexl (" + jexl.stackOverflow + ")", null);
            }
            cancelCheck(node);
            return node.jjtAccept(this, null);
        } catch(StackOverflowError xstack) {
            JexlException xjexl = new JexlException.StackOverflow(node.jexlInfo(), "jvm", xstack);
            if (!isSilent()) {
                throw xjexl.clean();
            }
            if (logger.isWarnEnabled()) {
                logger.warn(xjexl.getMessage(), xjexl.getCause());
            }
        } catch (JexlException.Return xreturn) {
            return xreturn.getValue();
        } catch (JexlException.Cancel xcancel) {
            // cancelled |= Thread.interrupted();
            cancelled.weakCompareAndSet(false, Thread.interrupted());
            if (isCancellable()) {
                throw xcancel.clean();
            }
        } catch (JexlException xjexl) {
            if (!isSilent()) {
                throw xjexl.clean();
            }
            if (logger.isWarnEnabled()) {
                logger.warn(xjexl.getMessage(), xjexl.getCause());
            }
        } finally {
            synchronized(this) {
                if (functors != null) {
                    if (AUTOCLOSEABLE != null) {
                        for (Object functor : functors.values()) {
                            closeIfSupported(functor);
                        }
                    }
                    functors.clear();
                    functors = null;
                }
            }
            jexl.putThreadEngine(tjexl);
            if (context instanceof JexlContext.ThreadLocal) {
                jexl.putThreadLocal(tcontext);
            }
            if (tinter != null) {
                fp = tinter.fp - 1;
            }
            putThreadInterpreter(tinter);
        }
        return null;
    }

    /**
     * Gets an attribute of an object.
     *
     * @param object    to retrieve value from
     * @param attribute the attribute of the object, e.g. an index (1, 0, 2) or key for a map
     * @return the attribute value
     */
    public Object getAttribute(Object object, Object attribute) {
        return getAttribute(object, attribute, null);
    }
    /**
     * Sets an attribute of an object.
     *
     * @param object    to set the value to
     * @param attribute the attribute of the object, e.g. an index (1, 0, 2) or key for a map
     * @param value     the value to assign to the object's attribute
     */
    public void setAttribute(Object object, Object attribute, Object value) {
        setAttribute(object, attribute, value, null);
    }

    @Override
    protected Object visit(ASTAddNode node, Object data) {
        Object left = node.jjtGetChild(0).jjtAccept(this, data);
        Object right = node.jjtGetChild(1).jjtAccept(this, data);
        try {
            Object result = operators.tryOverload(node, JexlOperator.ADD, left, right);
            return result != JexlEngine.TRY_FAILED ? result : arithmetic.add(left, right);
        } catch (ArithmeticException xrt) {
            throw new JexlException(node, "+ error", xrt);
        }
    }

    @Override
    protected Object visit(ASTSubNode node, Object data) {
        Object left = node.jjtGetChild(0).jjtAccept(this, data);
        Object right = node.jjtGetChild(1).jjtAccept(this, data);
        try {
            Object result = operators.tryOverload(node, JexlOperator.SUBTRACT, left, right);
            return result != JexlEngine.TRY_FAILED ? result : arithmetic.subtract(left, right);
        } catch (ArithmeticException xrt) {
            throw new JexlException(node, "- error", xrt);
        }
    }

    @Override
    protected Object visit(ASTMulNode node, Object data) {
        Object left = node.jjtGetChild(0).jjtAccept(this, data);
        Object right = node.jjtGetChild(1).jjtAccept(this, data);
        try {
            Object result = operators.tryOverload(node, JexlOperator.MULTIPLY, left, right);
            return result != JexlEngine.TRY_FAILED ? result : arithmetic.multiply(left, right);
        } catch (ArithmeticException xrt) {
            JexlNode xnode = findNullOperand(xrt, node, left, right);
            throw new JexlException(xnode, "* error", xrt);
        }
    }

    @Override
    protected Object visit(ASTDivNode node, Object data) {
        Object left = node.jjtGetChild(0).jjtAccept(this, data);
        Object right = node.jjtGetChild(1).jjtAccept(this, data);
        try {
            Object result = operators.tryOverload(node, JexlOperator.DIVIDE, left, right);
            return result != JexlEngine.TRY_FAILED ? result : arithmetic.divide(left, right);
        } catch (ArithmeticException xrt) {
            if (!arithmetic.isStrict()) {
                return 0.0d;
            }
            JexlNode xnode = findNullOperand(xrt, node, left, right);
            throw new JexlException(xnode, "/ error", xrt);
        }
    }

    @Override
    protected Object visit(ASTModNode node, Object data) {
        Object left = node.jjtGetChild(0).jjtAccept(this, data);
        Object right = node.jjtGetChild(1).jjtAccept(this, data);
        try {
            Object result = operators.tryOverload(node, JexlOperator.MOD, left, right);
            return result != JexlEngine.TRY_FAILED ? result : arithmetic.mod(left, right);
        } catch (ArithmeticException xrt) {
            if (!arithmetic.isStrict()) {
                return 0.0d;
            }
            JexlNode xnode = findNullOperand(xrt, node, left, right);
            throw new JexlException(xnode, "% error", xrt);
        }
    }

    @Override
    protected Object visit(ASTBitwiseAndNode node, Object data) {
        Object left = node.jjtGetChild(0).jjtAccept(this, data);
        Object right = node.jjtGetChild(1).jjtAccept(this, data);
        try {
            Object result = operators.tryOverload(node, JexlOperator.AND, left, right);
            return result != JexlEngine.TRY_FAILED ? result : arithmetic.and(left, right);
        } catch (ArithmeticException xrt) {
            throw new JexlException(node, "& error", xrt);
        }
    }

    @Override
    protected Object visit(ASTBitwiseOrNode node, Object data) {
        Object left = node.jjtGetChild(0).jjtAccept(this, data);
        Object right = node.jjtGetChild(1).jjtAccept(this, data);
        try {
            Object result = operators.tryOverload(node, JexlOperator.OR, left, right);
            return result != JexlEngine.TRY_FAILED ? result : arithmetic.or(left, right);
        } catch (ArithmeticException xrt) {
            throw new JexlException(node, "| error", xrt);
        }
    }

    @Override
    protected Object visit(ASTBitwiseXorNode node, Object data) {
        Object left = node.jjtGetChild(0).jjtAccept(this, data);
        Object right = node.jjtGetChild(1).jjtAccept(this, data);
        try {
            Object result = operators.tryOverload(node, JexlOperator.XOR, left, right);
            return result != JexlEngine.TRY_FAILED ? result : arithmetic.xor(left, right);
        } catch (ArithmeticException xrt) {
            throw new JexlException(node, "^ error", xrt);
        }
    }

    @Override
    protected Object visit(ASTEQNode node, Object data) {
        Object left = node.jjtGetChild(0).jjtAccept(this, data);
        Object right = node.jjtGetChild(1).jjtAccept(this, data);
        try {
            Object result = operators.tryOverload(node, JexlOperator.EQ, left, right);
            return result != JexlEngine.TRY_FAILED
                   ? result
                   : arithmetic.equals(left, right) ? Boolean.TRUE : Boolean.FALSE;
        } catch (ArithmeticException xrt) {
            throw new JexlException(node, "== error", xrt);
        }
    }

    @Override
    protected Object visit(ASTNENode node, Object data) {
        Object left = node.jjtGetChild(0).jjtAccept(this, data);
        Object right = node.jjtGetChild(1).jjtAccept(this, data);
        try {
            Object result = operators.tryOverload(node, JexlOperator.EQ, left, right);
            return result != JexlEngine.TRY_FAILED
                   ? arithmetic.toBoolean(result) ? Boolean.FALSE : Boolean.TRUE
                   : arithmetic.equals(left, right) ? Boolean.FALSE : Boolean.TRUE;
        } catch (ArithmeticException xrt) {
            JexlNode xnode = findNullOperand(xrt, node, left, right);
            throw new JexlException(xnode, "!= error", xrt);
        }
    }

    @Override
    protected Object visit(ASTGENode node, Object data) {
        Object left = node.jjtGetChild(0).jjtAccept(this, data);
        Object right = node.jjtGetChild(1).jjtAccept(this, data);
        try {
            Object result = operators.tryOverload(node, JexlOperator.GTE, left, right);
            return result != JexlEngine.TRY_FAILED
                   ? result
                   : arithmetic.greaterThanOrEqual(left, right) ? Boolean.TRUE : Boolean.FALSE;
        } catch (ArithmeticException xrt) {
            throw new JexlException(node, ">= error", xrt);
        }
    }

    @Override
    protected Object visit(ASTGTNode node, Object data) {
        Object left = node.jjtGetChild(0).jjtAccept(this, data);
        Object right = node.jjtGetChild(1).jjtAccept(this, data);
        try {
            Object result = operators.tryOverload(node, JexlOperator.GT, left, right);
            return result != JexlEngine.TRY_FAILED
                   ? result
                   : arithmetic.greaterThan(left, right) ? Boolean.TRUE : Boolean.FALSE;
        } catch (ArithmeticException xrt) {
            throw new JexlException(node, "> error", xrt);
        }
    }

    @Override
    protected Object visit(ASTLENode node, Object data) {
        Object left = node.jjtGetChild(0).jjtAccept(this, data);
        Object right = node.jjtGetChild(1).jjtAccept(this, data);
        try {
            Object result = operators.tryOverload(node, JexlOperator.LTE, left, right);
            return result != JexlEngine.TRY_FAILED
                   ? result
                   : arithmetic.lessThanOrEqual(left, right) ? Boolean.TRUE : Boolean.FALSE;
        } catch (ArithmeticException xrt) {
            throw new JexlException(node, "<= error", xrt);
        }
    }

    @Override
    protected Object visit(ASTLTNode node, Object data) {
        Object left = node.jjtGetChild(0).jjtAccept(this, data);
        Object right = node.jjtGetChild(1).jjtAccept(this, data);
        try {
            Object result = operators.tryOverload(node, JexlOperator.LT, left, right);
            return result != JexlEngine.TRY_FAILED
                   ? result
                   : arithmetic.lessThan(left, right) ? Boolean.TRUE : Boolean.FALSE;
        } catch (ArithmeticException xrt) {
            throw new JexlException(node, "< error", xrt);
        }
    }

    @Override
    protected Object visit(ASTSWNode node, Object data) {
        Object left = node.jjtGetChild(0).jjtAccept(this, data);
        Object right = node.jjtGetChild(1).jjtAccept(this, data);
        return operators.startsWith(node, "^=", left, right) ? Boolean.TRUE : Boolean.FALSE;
    }

    @Override
    protected Object visit(ASTNSWNode node, Object data) {
        Object left = node.jjtGetChild(0).jjtAccept(this, data);
        Object right = node.jjtGetChild(1).jjtAccept(this, data);
        return operators.startsWith(node, "^!", left, right) ? Boolean.FALSE : Boolean.TRUE;
    }

    @Override
    protected Object visit(ASTEWNode node, Object data) {
        Object left = node.jjtGetChild(0).jjtAccept(this, data);
        Object right = node.jjtGetChild(1).jjtAccept(this, data);
        return operators.endsWith(node, "$=", left, right) ? Boolean.TRUE : Boolean.FALSE;
    }

    @Override
    protected Object visit(ASTNEWNode node, Object data) {
        Object left = node.jjtGetChild(0).jjtAccept(this, data);
        Object right = node.jjtGetChild(1).jjtAccept(this, data);
        return operators.endsWith(node, "$!", left, right) ? Boolean.FALSE : Boolean.TRUE;
    }

    @Override
    protected Object visit(ASTERNode node, Object data) {
        Object left = node.jjtGetChild(0).jjtAccept(this, data);
        Object right = node.jjtGetChild(1).jjtAccept(this, data);
        return operators.contains(node, "=~", right, left) ? Boolean.TRUE : Boolean.FALSE;
    }

    @Override
    protected Object visit(ASTNRNode node, Object data) {
        Object left = node.jjtGetChild(0).jjtAccept(this, data);
        Object right = node.jjtGetChild(1).jjtAccept(this, data);
        return operators.contains(node, "!~", right, left) ? Boolean.FALSE : Boolean.TRUE;
    }

    @Override
    protected Object visit(ASTRangeNode node, Object data) {
        Object left = node.jjtGetChild(0).jjtAccept(this, data);
        Object right = node.jjtGetChild(1).jjtAccept(this, data);
        try {
            return arithmetic.createRange(left, right);
        } catch (ArithmeticException xrt) {
            JexlNode xnode = findNullOperand(xrt, node, left, right);
            throw new JexlException(xnode, ".. error", xrt);
        }
    }

    @Override
    protected Object visit(ASTUnaryMinusNode node, Object data) {
        // use cached value if literal
        Object value = node.jjtGetValue();
        if (value != null && !(value instanceof JexlMethod)) {
            return value;
        }
        JexlNode valNode = node.jjtGetChild(0);
        Object val = valNode.jjtAccept(this, data);
        try {
            Object result = operators.tryOverload(node, JexlOperator.NEGATE, val);
            if (result != JexlEngine.TRY_FAILED) {
                return result;
            }
            Object number = arithmetic.negate(val);
            // attempt to recoerce to literal class
            if ((number instanceof Number)) {
                // cache if number literal and negate is idempotent
                if (valNode instanceof ASTNumberLiteral) {
                    number = arithmetic.narrowNumber((Number) number, ((ASTNumberLiteral) valNode).getLiteralClass());
                    if (arithmetic.isNegateStable()) {
                        node.jjtSetValue(number);
                    }
                }
            }
            return number;
        } catch (ArithmeticException xrt) {
            throw new JexlException(valNode, "- error", xrt);
        }
    }

    @Override
    protected Object visit(ASTUnaryPlusNode node, Object data) {
        // use cached value if literal
        Object value = node.jjtGetValue();
        if (value != null && !(value instanceof JexlMethod)) {
            return value;
        }
        JexlNode valNode = node.jjtGetChild(0);
        Object val = valNode.jjtAccept(this, data);
        try {
            Object result = operators.tryOverload(node, JexlOperator.POSITIVIZE, val);
            if (result != JexlEngine.TRY_FAILED) {
                return result;
            }
            Object number = arithmetic.positivize(val);
            if (valNode instanceof ASTNumberLiteral
                && number instanceof Number
                && arithmetic.isPositivizeStable()) {
                node.jjtSetValue(number);
            }
            return number;
        } catch (ArithmeticException xrt) {
            throw new JexlException(valNode, "- error", xrt);
        }
    }

    @Override
    protected Object visit(ASTBitwiseComplNode node, Object data) {
        Object arg = node.jjtGetChild(0).jjtAccept(this, data);
        try {
            Object result = operators.tryOverload(node, JexlOperator.COMPLEMENT, arg);
            return result != JexlEngine.TRY_FAILED ? result : arithmetic.complement(arg);
        } catch (ArithmeticException xrt) {
            throw new JexlException(node, "~ error", xrt);
        }
    }

    @Override
    protected Object visit(ASTNotNode node, Object data) {
        Object val = node.jjtGetChild(0).jjtAccept(this, data);
        try {
            Object result = operators.tryOverload(node, JexlOperator.NOT, val);
            return result != JexlEngine.TRY_FAILED ? result : arithmetic.not(val);
        } catch (ArithmeticException xrt) {
            throw new JexlException(node, "! error", xrt);
        }
    }

    @Override
    protected Object visit(ASTIfStatement node, Object data) {
        int n = 0;
        final int numChildren = node.jjtGetNumChildren();
        try {
            Object result = null;
            // pairs of { conditions , 'then' statement }
            for(int ifElse = 0; ifElse < (numChildren - 1); ifElse += 2) {
                Object condition = node.jjtGetChild(ifElse).jjtAccept(this, null);
                if (arithmetic.toBoolean(condition)) {
                    // first objectNode is true statement
                    return node.jjtGetChild(ifElse + 1).jjtAccept(this, null);
                }
            }
            // if odd...
            if ((numChildren & 1) == 1) {
                // if there is an else, there are an odd number of children in the statement and it is the last child,
                // execute it.
                result = node.jjtGetChild(numChildren - 1).jjtAccept(this, null);
            }
            return result;
        } catch (ArithmeticException xrt) {
            throw new JexlException(node.jjtGetChild(n), "if error", xrt);
        }
    }

    @Override
    protected Object visit(ASTVar node, Object data) {
        int symbol = node.getSymbol();
        // if we have a var, we have a scope thus a frame
        if (!options.isLexical()) {
            if (frame.has(symbol)) {
                return frame.get(symbol);
            }
        } else if (!block.declareSymbol(symbol)) {
            return redefinedVariable(node, node.getName());
        }
        frame.set(symbol, null);
        return null;
    }

    @Override
    protected Object visit(ASTBlock node, Object data) {
        int cnt = node.getSymbolCount();
        if (!options.isLexical() || cnt <= 0) {
            return visitBlock(node, data);
        }
        try {
            block = new LexicalFrame(frame, block);
            return visitBlock(node, data);
        } finally {
            block = block.pop();
        }
    }

    /**
     * Base visitation for blocks.
     * @param node the block
     * @param data the usual data
     * @return the result of the last expression evaluation
     */
    private Object visitBlock(ASTBlock node, Object data) {
        int numChildren = node.jjtGetNumChildren();
        Object result = null;
        for (int i = 0; i < numChildren; i++) {
            cancelCheck(node);
            result = node.jjtGetChild(i).jjtAccept(this, data);
        }
        return result;
    }

    @Override
    protected Object visit(ASTReturnStatement node, Object data) {
        Object val = node.jjtGetChild(0).jjtAccept(this, data);
        cancelCheck(node);
        throw new JexlException.Return(node, null, val);
    }

    @Override
    protected Object visit(ASTContinue node, Object data) {
        throw new JexlException.Continue(node);
    }

    @Override
    protected Object visit(ASTBreak node, Object data) {
        throw new JexlException.Break(node);
    }

    @Override
    protected Object visit(ASTForeachStatement node, Object data) {
        Object result = null;
        /* first objectNode is the loop variable */
        ASTReference loopReference = (ASTReference) node.jjtGetChild(0);
        ASTIdentifier loopVariable = (ASTIdentifier) loopReference.jjtGetChild(0);
        final int symbol = loopVariable.getSymbol();
        final boolean lexical = options.isLexical();// && node.getSymbolCount() > 0;
        final boolean loopSymbol = symbol >= 0 && loopVariable instanceof ASTVar;
        if (lexical) {
            // create lexical frame
            LexicalFrame locals = new LexicalFrame(frame, block);
            // it may be a local previously declared
            if (loopSymbol && !locals.declareSymbol(symbol)) {
                return redefinedVariable(node, loopVariable.getName());
            }
            block = locals;
        }
        Object forEach = null;
        try {
            /* second objectNode is the variable to iterate */
            Object iterableValue = node.jjtGetChild(1).jjtAccept(this, data);
            // make sure there is a value to iterate upon
            if (iterableValue != null) {
                /* third objectNode is the statement to execute */
                JexlNode statement = node.jjtGetNumChildren() >= 3 ? node.jjtGetChild(2) : null;
                // get an iterator for the collection/array etc via the introspector.
                forEach = operators.tryOverload(node, JexlOperator.FOR_EACH, iterableValue);
                Iterator<?> itemsIterator = forEach instanceof Iterator
                        ? (Iterator<?>) forEach
                        : uberspect.getIterator(iterableValue);
                if (itemsIterator != null) {
                    int cnt = 0;
                    while (itemsIterator.hasNext()) {
                        cancelCheck(node);
                        // reset loop varaible
                        if (lexical && cnt++ > 0) {
                            // clean up but remain current
                            block.pop();
                            // unlikely to fail 
                            if (loopSymbol && !block.declareSymbol(symbol)) {
                                return redefinedVariable(node, loopVariable.getName());
                            }
                        }
                        // set loopVariable to value of iterator
                        Object value = itemsIterator.next();
                        if (symbol < 0) {
                            setContextVariable(node, loopVariable.getName(), value);
                        } else {
                            frame.set(symbol, value);
                        }
                        if (statement != null) {
                            try {
                                // execute statement
                                result = statement.jjtAccept(this, data);
                            } catch (JexlException.Break stmtBreak) {
                                break;
                            } catch (JexlException.Continue stmtContinue) {
                                //continue;
                            }
                        }
                    }
                }
            }
        } finally {
            //  closeable iterator handling
            closeIfSupported(forEach);
            // restore lexical frame
            if (lexical) {
                block = block.pop();
            }
        }
        return result;
    }

    @Override
    protected Object visit(ASTWhileStatement node, Object data) {
        Object result = null;
        /* first objectNode is the condition */
        Node condition = node.jjtGetChild(0);
        while (arithmetic.toBoolean(condition.jjtAccept(this, data))) {
            cancelCheck(node);
            if (node.jjtGetNumChildren() > 1) {
                try {
                    // execute statement
                    result = node.jjtGetChild(1).jjtAccept(this, data);
                } catch (JexlException.Break stmtBreak) {
                    break;
                } catch (JexlException.Continue stmtContinue) {
                    //continue;
                }
            }
        }
        return result;
    }

    @Override
    protected Object visit(ASTDoWhileStatement node, Object data) {
        Object result = null;
        int nc = node.jjtGetNumChildren();
        /* last objectNode is the condition */
        Node condition = node.jjtGetChild(nc - 1);
        do {
            cancelCheck(node);
            if (nc > 1) {
                try {
                    // execute statement
                    result = node.jjtGetChild(0).jjtAccept(this, data);
                } catch (JexlException.Break stmtBreak) {
                    break;
                } catch (JexlException.Continue stmtContinue) {
                    //continue;
                }
            }
        } while (arithmetic.toBoolean(condition.jjtAccept(this, data)));
        return result;
    }

    @Override
    protected Object visit(ASTAndNode node, Object data) {
        /**
         * The pattern for exception mgmt is to let the child*.jjtAccept out of the try/catch loop so that if one fails,
         * the ex will traverse up to the interpreter. In cases where this is not convenient/possible, JexlException
         * must be caught explicitly and rethrown.
         */
        Object left = node.jjtGetChild(0).jjtAccept(this, data);
        try {
            boolean leftValue = arithmetic.toBoolean(left);
            if (!leftValue) {
                return Boolean.FALSE;
            }
        } catch (ArithmeticException xrt) {
            throw new JexlException(node.jjtGetChild(0), "boolean coercion error", xrt);
        }
        Object right = node.jjtGetChild(1).jjtAccept(this, data);
        try {
            boolean rightValue = arithmetic.toBoolean(right);
            if (!rightValue) {
                return Boolean.FALSE;
            }
        } catch (ArithmeticException xrt) {
            throw new JexlException(node.jjtGetChild(1), "boolean coercion error", xrt);
        }
        return Boolean.TRUE;
    }

    @Override
    protected Object visit(ASTOrNode node, Object data) {
        Object left = node.jjtGetChild(0).jjtAccept(this, data);
        try {
            boolean leftValue = arithmetic.toBoolean(left);
            if (leftValue) {
                return Boolean.TRUE;
            }
        } catch (ArithmeticException xrt) {
            throw new JexlException(node.jjtGetChild(0), "boolean coercion error", xrt);
        }
        Object right = node.jjtGetChild(1).jjtAccept(this, data);
        try {
            boolean rightValue = arithmetic.toBoolean(right);
            if (rightValue) {
                return Boolean.TRUE;
            }
        } catch (ArithmeticException xrt) {
            throw new JexlException(node.jjtGetChild(1), "boolean coercion error", xrt);
        }
        return Boolean.FALSE;
    }

    @Override
    protected Object visit(ASTNullLiteral node, Object data) {
        return null;
    }

    @Override
    protected Object visit(ASTTrueNode node, Object data) {
        return Boolean.TRUE;
    }

    @Override
    protected Object visit(ASTFalseNode node, Object data) {
        return Boolean.FALSE;
    }

    @Override
    protected Object visit(ASTNumberLiteral node, Object data) {
        if (data != null && node.isInteger()) {
            return getAttribute(data, node.getLiteral(), node);
        }
        return node.getLiteral();
    }

    @Override
    protected Object visit(ASTStringLiteral node, Object data) {
        if (data != null) {
            return getAttribute(data, node.getLiteral(), node);
        }
        return node.getLiteral();
    }

    @Override
    protected Object visit(ASTRegexLiteral node, Object data) {
        return node.getLiteral();
    }

    @Override
    protected Object visit(ASTArrayLiteral node, Object data) {
        int childCount = node.jjtGetNumChildren();
        JexlArithmetic.ArrayBuilder ab = arithmetic.arrayBuilder(childCount);
        boolean extended = false;
        for (int i = 0; i < childCount; i++) {
            cancelCheck(node);
            JexlNode child = node.jjtGetChild(i);
            if (child instanceof ASTExtendedLiteral) {
                extended = true;
            } else {
                Object entry = node.jjtGetChild(i).jjtAccept(this, data);
                ab.add(entry);
            }
        }
        return ab.create(extended);
    }

    @Override
    protected Object visit(ASTExtendedLiteral node, Object data) {
        return node;
    }

    @Override
    protected Object visit(ASTSetLiteral node, Object data) {
        int childCount = node.jjtGetNumChildren();
        JexlArithmetic.SetBuilder mb = arithmetic.setBuilder(childCount);
        for (int i = 0; i < childCount; i++) {
            cancelCheck(node);
            Object entry = node.jjtGetChild(i).jjtAccept(this, data);
            mb.add(entry);
        }
        return mb.create();
    }

    @Override
    protected Object visit(ASTMapLiteral node, Object data) {
        int childCount = node.jjtGetNumChildren();
        JexlArithmetic.MapBuilder mb = arithmetic.mapBuilder(childCount);
        for (int i = 0; i < childCount; i++) {
            cancelCheck(node);
            Object[] entry = (Object[]) (node.jjtGetChild(i)).jjtAccept(this, data);
            mb.put(entry[0], entry[1]);
        }
        return mb.create();
    }

    @Override
    protected Object visit(ASTMapEntry node, Object data) {
        Object key = node.jjtGetChild(0).jjtAccept(this, data);
        Object value = node.jjtGetChild(1).jjtAccept(this, data);
        return new Object[]{key, value};
    }

    @Override
    protected Object visit(ASTTernaryNode node, Object data) {
        Object condition;
        try {
            condition = node.jjtGetChild(0).jjtAccept(this, data);
        } catch(JexlException xany) {
            if (!(xany.getCause() instanceof JexlArithmetic.NullOperand)) {
                throw xany;
            }
            condition = null;
        }
        // ternary as in "x ? y : z"
        if (node.jjtGetNumChildren() == 3) {
            if (condition != null && arithmetic.toBoolean(condition)) {
                return node.jjtGetChild(1).jjtAccept(this, data);
            } else {
                return node.jjtGetChild(2).jjtAccept(this, data);
            }
        }
        // elvis as in "x ?: z"
        if (condition != null && arithmetic.toBoolean(condition)) {
            return condition;
        } else {
            return node.jjtGetChild(1).jjtAccept(this, data);
        }
    }

    @Override
    protected Object visit(ASTNullpNode node, Object data) {
        Object lhs;
        try {
            lhs = node.jjtGetChild(0).jjtAccept(this, data);
        } catch(JexlException xany) {
            if (!(xany.getCause() instanceof JexlArithmetic.NullOperand)) {
                throw xany;
            }
            lhs = null;
        }
        // null elision as in "x ?? z"
        return lhs != null? lhs : node.jjtGetChild(1).jjtAccept(this, data);
    }

    @Override
    protected Object visit(ASTSizeFunction node, Object data) {
        try {
            Object val = node.jjtGetChild(0).jjtAccept(this, data);
            return operators.size(node, val);
        } catch(JexlException xany) {
            return 0;
        }
    }

    @Override
    protected Object visit(ASTEmptyFunction node, Object data) {
        try {
            Object value = node.jjtGetChild(0).jjtAccept(this, data);
            return operators.empty(node, value);
        } catch(JexlException xany) {
            return true;
        }
    }
    
    /**
     * Runs a node.
     * @param node the node
     * @param data the usual data
     * @return the return value
     */
    protected Object visitLexicalNode(JexlNode node, Object data) {
        block = new LexicalFrame(frame, null);
        try {
            return node.jjtAccept(this, data);
        } finally {
            block = block.pop();
        }
    }
    
    /**
     * Runs a closure.
     * @param closure the closure
     * @param data the usual data
     * @return the closure return value
     */
    protected Object runClosure(Closure closure, Object data) {
        ASTJexlScript script = closure.getScript();
        block = new LexicalFrame(frame, block).declareArgs();
        try {
            JexlNode body = script.jjtGetChild(script.jjtGetNumChildren() - 1);
            return interpret(body);
        } finally {
            block = block.pop();
        }
    }

    @Override
    protected Object visit(ASTJexlScript script, Object data) {
        if (script instanceof ASTJexlLambda && !((ASTJexlLambda) script).isTopLevel()) {
            return new Closure(this, (ASTJexlLambda) script);
        } else {
            block = new LexicalFrame(frame, block).declareArgs();
            try {
                final int numChildren = script.jjtGetNumChildren();
                Object result = null;
                for (int i = 0; i < numChildren; i++) {
                    JexlNode child = script.jjtGetChild(i);
                    result = child.jjtAccept(this, data);
                    cancelCheck(child);
                }
                return result;
            } finally {
                block = block.pop();
            }
        }
    }

    @Override
    protected Object visit(ASTReferenceExpression node, Object data) {
        return node.jjtGetChild(0).jjtAccept(this, data);
    }

    @Override
    protected Object visit(ASTIdentifier identifier, Object data) {
        cancelCheck(identifier);
        return data != null
                ? getAttribute(data, identifier.getName(), identifier)
                : getVariable(frame, block, identifier);
    }

    @Override
    protected Object visit(ASTArrayAccess node, Object data) {
        // first objectNode is the identifier
        Object object = data;
        // can have multiple nodes - either an expression, integer literal or reference
        int numChildren = node.jjtGetNumChildren();
        for (int i = 0; i < numChildren; i++) {
            JexlNode nindex = node.jjtGetChild(i);
            if (object == null) {
                return unsolvableProperty(nindex, stringifyProperty(nindex), false, null);
            }
            Object index = nindex.jjtAccept(this, null);
            cancelCheck(node);
            object = getAttribute(object, index, nindex);
        }
        return object;
    }

    /**
     * Evaluates an access identifier based on the 2 main implementations;
     * static (name or numbered identifier) or dynamic (jxlt).
     * @param node the identifier access node
     * @return the evaluated identifier
     */
    private Object evalIdentifier(ASTIdentifierAccess node) {
        if (node instanceof ASTIdentifierAccessJxlt) {
            final ASTIdentifierAccessJxlt accessJxlt = (ASTIdentifierAccessJxlt) node;
            final String src = node.getName();
            Throwable cause = null;
            TemplateEngine.TemplateExpression expr = (TemplateEngine.TemplateExpression) accessJxlt.getExpression();
            try {
                if (expr == null) {
                    TemplateEngine jxlt = jexl.jxlt();
                    expr = jxlt.parseExpression(node.jexlInfo(), src, frame != null ? frame.getScope() : null);
                    accessJxlt.setExpression(expr);
                }
                if (expr != null) {
                    Object name = expr.evaluate(frame, context);
                    if (name != null) {
                        Integer id = ASTIdentifierAccess.parseIdentifier(name.toString());
                        return id != null ? id : name;
                    }
                }
            } catch (JxltEngine.Exception xjxlt) {
                cause = xjxlt;
            }
            return node.isSafe() ? null : unsolvableProperty(node, src, true, cause);
        } else {
            return node.getIdentifier();
        }
    }

    @Override
    protected Object visit(ASTIdentifierAccess node, Object data) {
        if (data == null) {
            return null;
        }
        Object id = evalIdentifier(node);
        return getAttribute(data, id, node);
    }

    @Override
    protected Object visit(final ASTReference node, Object data) {
        cancelCheck(node);
        final int numChildren = node.jjtGetNumChildren();
        final JexlNode parent = node.jjtGetParent();
        // pass first piece of data in and loop through children
        Object object = null;
        JexlNode objectNode = null;
        JexlNode ptyNode = null;
        StringBuilder ant = null;
        boolean antish = !(parent instanceof ASTReference);
        int v = 1;
        main:
        for (int c = 0; c < numChildren; c++) {
            objectNode = node.jjtGetChild(c);
            if (objectNode instanceof ASTMethodNode) {
                if (object == null) {
                    // we may be performing a method call on an antish var
                    if (ant != null) {
                        JexlNode child = objectNode.jjtGetChild(0);
                        if (child instanceof ASTIdentifierAccess) {
                            int alen = ant.length();
                            ant.append('.');
                            ant.append(((ASTIdentifierAccess) child).getName());
                            object = context.get(ant.toString());
                            if (object != null) {
                                object = visit((ASTMethodNode) objectNode, object, context);
                                continue;
                            } else {
                                // remove method name from antish
                                ant.delete(alen, ant.length());
                            }
                        }
                    }
                    break;
                } else {
                    antish = false;
                }
            } else if (objectNode instanceof ASTArrayAccess) {
                if (object == null) {
                    ptyNode = objectNode;
                    break;
                } else {
                    antish = false;
                }
            }
            // attempt to evaluate the property within the object (visit(ASTIdentifierAccess node))
            object = objectNode.jjtAccept(this, object);
            cancelCheck(node);
            if (object != null) {
                // disallow mixing antish variable & bean with same root; avoid ambiguity
                antish = false;
            } else if (antish) {
                // create first from first node
                if (ant == null) {
                    // if we still have a null object, check for an antish variable
                    JexlNode first = node.jjtGetChild(0);
                    if (first instanceof ASTIdentifier) {
                        ASTIdentifier afirst = (ASTIdentifier) first;
                        ant = new StringBuilder(afirst.getName());
                        // skip the else...*
                    } else {
                        // not an identifier, not antish
                        ptyNode = objectNode;
                        break main;
                    }
                    // *... and continue
                    if (!options.isAntish()) {
                        antish = false;
                        continue;
                    }
                    // skip the first node case since it was trialed in jjtAccept above and returned null
                    if (c == 0) {
                        continue;
                    }
                }
                // catch up to current node
                for (; v <= c; ++v) {
                    JexlNode child = node.jjtGetChild(v);
                    if (child instanceof ASTIdentifierAccess) {
                        ASTIdentifierAccess achild = (ASTIdentifierAccess) child;
                        if (achild.isSafe() || achild.isExpression()) {
                            break main;
                        }
                        ant.append('.');
                        ant.append(achild.getName());
                    } else {
                        // not an identifier, not antish
                        ptyNode = objectNode;
                        break main;
                    }
                }
                // solve antish
                object = context.get(ant.toString());
            } else if (c != numChildren - 1) {
                // only the last one may be null
                ptyNode = objectNode;
                break; //
            }
        }
        // am I the left-hand side of a safe op ?
        if (object == null) {
            if (ptyNode != null) {
                if (ptyNode.isSafeLhs(isSafe())) {
                    return null;
                }
                if (ant != null) {
                    String aname = ant.toString();
                    boolean undefined = !(context.has(aname) || isLocalVariable(node, 0) || isFunctionCall(node));
                    return unsolvableVariable(node, aname, undefined);
                }
                return unsolvableProperty(node, stringifyProperty(ptyNode), ptyNode == objectNode, null);
            }
            if (antish) {
                if (node.isSafeLhs(isSafe())) {
                    return null;
                }
                String aname = ant != null ? ant.toString() : "?";
                boolean undefined = !(context.has(aname) || isLocalVariable(node, 0) || isFunctionCall(node));
                if (undefined || arithmetic.isStrict()) {
                    return unsolvableVariable(node, aname, undefined);
                }
            }
        }
        return object;
    }

    @Override
    protected Object visit(ASTAssignment node, Object data) {
        return executeAssign(node, null, data);
    }

    @Override
    protected Object visit(ASTSetAddNode node, Object data) {
        return executeAssign(node, JexlOperator.SELF_ADD, data);
    }

    @Override
    protected Object visit(ASTSetSubNode node, Object data) {
        return executeAssign(node, JexlOperator.SELF_SUBTRACT, data);
    }

    @Override
    protected Object visit(ASTSetMultNode node, Object data) {
        return executeAssign(node, JexlOperator.SELF_MULTIPLY, data);
    }

    @Override
    protected Object visit(ASTSetDivNode node, Object data) {
        return executeAssign(node, JexlOperator.SELF_DIVIDE, data);
    }

    @Override
    protected Object visit(ASTSetModNode node, Object data) {
        return executeAssign(node, JexlOperator.SELF_MOD, data);
    }

    @Override
    protected Object visit(ASTSetAndNode node, Object data) {
        return executeAssign(node, JexlOperator.SELF_AND, data);
    }

    @Override
    protected Object visit(ASTSetOrNode node, Object data) {
        return executeAssign(node, JexlOperator.SELF_OR, data);
    }

    @Override
    protected Object visit(ASTSetXorNode node, Object data) {
        return executeAssign(node, JexlOperator.SELF_XOR, data);
    }

    /**
     * Executes an assignment with an optional side-effect operator.
     * @param node     the node
     * @param assignop the assignment operator or null if simply assignment
     * @param data     the data
     * @return the left hand side
     */
    protected Object executeAssign(JexlNode node, JexlOperator assignop, Object data) { // CSOFF: MethodLength
        cancelCheck(node);
        // left contains the reference to assign to
        final JexlNode left = node.jjtGetChild(0);
        ASTIdentifier var = null;
        Object object = null;
        int symbol = -1;
        // check var decl with assign is ok
        if (left instanceof ASTIdentifier) {
            var = (ASTIdentifier) left;
            symbol = var.getSymbol();
            if (symbol >= 0 && options.isLexical()) {
                if (var instanceof ASTVar) {
                    if (!block.declareSymbol(symbol)) {
                        return redefinedVariable(var, var.getName());
                    }
                } else if (isSymbolShaded(symbol, block)) { 
                    return undefinedVariable(var, var.getName());
                }
            }
        }
        boolean antish = options.isAntish();
        // 0: determine initial object & property:
        final int last = left.jjtGetNumChildren() - 1;
        // right is the value expression to assign
        Object right = node.jjtGetChild(1).jjtAccept(this, data);
        // a (var?) v = ... expression
        if (var != null) {
            if (symbol >= 0) {
                // check we are not assigning a symbol itself
                if (last < 0) {
                    if (assignop != null) {
                        Object self = getVariable(frame, block, var);
                        right = operators.tryAssignOverload(node, assignop, self, right);
                        if (right == JexlOperator.ASSIGN) {
                            return self;
                        }
                    }
                    frame.set(symbol, right);
                    // make the closure accessible to itself, ie hoist the currently set variable after frame creation
                    if (right instanceof Closure) {
                        ((Closure) right).setHoisted(symbol, right);
                    }
                    return right; // 1
                }
                object = getVariable(frame, block, var);
                // top level is a symbol, can not be an antish var
                antish = false;
            } else {
                // check we are not assigning direct global
                if (last < 0) {
                    if (assignop != null) {
                        Object self = context.get(var.getName());
                        right = operators.tryAssignOverload(node, assignop, self, right);
                        if (right == JexlOperator.ASSIGN) {
                            return self;
                        }
                    }
                    setContextVariable(node, var.getName(), right);
                    return right; // 2
                }
                object = context.get(var.getName());
                // top level accesses object, can not be an antish var
                if (object != null) {
                    antish = false;
                }
            }
        } else if (!(left instanceof ASTReference)) {
            throw new JexlException(left, "illegal assignment form 0");
        }
        // 1: follow children till penultimate, resolve dot/array
        JexlNode objectNode = null;
        StringBuilder ant = null;
        int v = 1;
        // start at 1 if symbol
        main: for (int c = symbol >= 0 ? 1 : 0; c < last; ++c) {
            objectNode = left.jjtGetChild(c);
            object = objectNode.jjtAccept(this, object);
            if (object != null) {
                // disallow mixing antish variable & bean with same root; avoid ambiguity
                antish = false;
            } else if (antish) {
                // initialize if first time
                if (ant == null) {
                    JexlNode first = left.jjtGetChild(0);
                    ASTIdentifier firstId = first instanceof ASTIdentifier
                            ? (ASTIdentifier) first
                            : null;
                    if (firstId != null && firstId.getSymbol() < 0) {
                        ant = new StringBuilder(firstId.getName());
                    } else {
                        // ant remains null, object is null, stop solving
                        antish = false;
                        break main;
                    }
                }
                // catch up to current child
                for (; v <= c; ++v) {
                    JexlNode child = left.jjtGetChild(v);
                    ASTIdentifierAccess aid = child instanceof ASTIdentifierAccess
                            ? (ASTIdentifierAccess) child
                            : null;
                    // remain antish only if unsafe navigation
                    if (aid != null && !aid.isSafe() && !aid.isExpression()) {
                        ant.append('.');
                        ant.append(aid.getName());
                    } else {
                        antish = false;
                        break main;
                    }
                }
                // solve antish
                object = context.get(ant.toString());
            } else {
                throw new JexlException(objectNode, "illegal assignment form");
            }
        }
        // 2: last objectNode will perform assignement in all cases
        Object property = null;
        JexlNode propertyNode = left.jjtGetChild(last);
        ASTIdentifierAccess propertyId = propertyNode instanceof ASTIdentifierAccess
                ? (ASTIdentifierAccess) propertyNode
                : null;
        if (propertyId != null) {
            // deal with creating/assignining antish variable
            if (antish && ant != null && object == null && !propertyId.isSafe() && !propertyId.isExpression()) {
                if (last > 0) {
                    ant.append('.');
                }
                ant.append(propertyId.getName());
                if (assignop != null) {
                    Object self = context.get(ant.toString());
                    right = operators.tryAssignOverload(node, assignop, self, right);
                    if (right == JexlOperator.ASSIGN) {
                        return self;
                    }
                }
                setContextVariable(propertyNode, ant.toString(), right);
                return right; // 3
            }
            // property of an object ?
            property = evalIdentifier(propertyId);
        } else if (propertyNode instanceof ASTArrayAccess) {
            // can have multiple nodes - either an expression, integer literal or reference
            int numChildren = propertyNode.jjtGetNumChildren() - 1;
            for (int i = 0; i < numChildren; i++) {
                JexlNode nindex = propertyNode.jjtGetChild(i);
                Object index = nindex.jjtAccept(this, null);
                object = getAttribute(object, index, nindex);
            }
            propertyNode = propertyNode.jjtGetChild(numChildren);
            property = propertyNode.jjtAccept(this, null);
        } else {
            throw new JexlException(objectNode, "illegal assignment form");
        }
        if (property == null) {
            // no property, we fail
            return unsolvableProperty(propertyNode, "<?>.<null>", true, null);
        }
        if (object == null) {
            // no object, we fail
            return unsolvableProperty(objectNode, "<null>.<?>", true, null);
        }
        // 3: one before last, assign
        if (assignop != null) {
            Object self = getAttribute(object, property, propertyNode);
            right = operators.tryAssignOverload(node, assignop, self, right);
            if (right == JexlOperator.ASSIGN) {
                return self;
            }
        }
        setAttribute(object, property, right, propertyNode);
        return right; // 4
    }

    @Override
    protected Object[] visit(ASTArguments node, Object data) {
        final int argc = node.jjtGetNumChildren();
        final Object[] argv = new Object[argc];
        for (int i = 0; i < argc; i++) {
            argv[i] = node.jjtGetChild(i).jjtAccept(this, data);
        }
        return argv;
    }

    @Override
    protected Object visit(final ASTMethodNode node, Object data) {
        return visit(node, null, data);
    }

    /**
     * Execute a method call, ie syntactically written as name.call(...).
     * @param node the actual method call node
     * @param object non null when name.call is an antish variable
     * @param data the context
     * @return the method call result
     */
    private Object visit(final ASTMethodNode node, Object object, Object data) {
        // left contains the reference to the method
        final JexlNode methodNode = node.jjtGetChild(0);
        Object method;
        // 1: determine object and method or functor
        if (methodNode instanceof ASTIdentifierAccess) {
            method = methodNode;
            if (object == null) {
                object = data;
                if (object == null) {
                    // no object, we fail
                    return node.isSafeLhs(isSafe())
                        ? null
                        : unsolvableMethod(methodNode, "<null>.<?>(...)");
                }
            } else {
                // edge case of antish var used as functor
                method = object;
            }
        } else {
            method = methodNode.jjtAccept(this, data);
        }
        Object result = method;
        for (int a = 1; a < node.jjtGetNumChildren(); ++a) {
            if (result == null) {
                // no method, we fail// variable unknown in context and not a local
                return node.isSafeLhs(isSafe())
                        ? null
                        : unsolvableMethod(methodNode, "<?>.<null>(...)");
            }
            ASTArguments argNode = (ASTArguments) node.jjtGetChild(a);
            result = call(node, object, result, argNode);
            object = result;
        }
        return result;
    }

    @Override
    protected Object visit(ASTFunctionNode node, Object data) {
        ASTIdentifier functionNode = (ASTIdentifier) node.jjtGetChild(0);
        String nsid = functionNode.getNamespace();
        Object namespace = (nsid != null)? resolveNamespace(nsid, node) : context;
        ASTArguments argNode = (ASTArguments) node.jjtGetChild(1);
        return call(node, namespace, functionNode, argNode);
    }

    /**
     * Calls a method (or function).
     * <p>
     * Method resolution is a follows:
     * 1 - attempt to find a method in the target passed as parameter;
     * 2 - if this fails, seeks a JexlScript or JexlMethod or a duck-callable* as a property of that target;
     * 3 - if this fails, narrow the arguments and try again 1
     * 4 - if this fails, seeks a context or arithmetic method with the proper name taking the target as first argument;
     * </p>
     * *duck-callable: an object where a "call" function exists
     *
     * @param node    the method node
     * @param target  the target of the method, what it should be invoked upon
     * @param functor the object carrying the method or function or the method identifier
     * @param argNode the node carrying the arguments
     * @return the result of the method invocation
     */
    protected Object call(final JexlNode node, Object target, Object functor, final ASTArguments argNode) {
        cancelCheck(node);
        // evaluate the arguments
        final Object[] argv = visit(argNode, null);
        // get the method name if identifier
        final int symbol;
        final String methodName;
        boolean cacheable = cache;
        boolean isavar = false;
        if (functor instanceof ASTIdentifier) {
            // function call, target is context or namespace (if there was one)
            ASTIdentifier methodIdentifier = (ASTIdentifier) functor;
            symbol = methodIdentifier.getSymbol();
            methodName = methodIdentifier.getName();
            functor = null;
            // is it a global or local variable ?
            if (target == context) {
                if (frame != null && frame.has(symbol)) {
                    functor = frame.get(symbol);
                    isavar = functor != null;
                } else if (context.has(methodName)) {
                    functor = context.get(methodName);
                    isavar = functor != null;
                }
                // name is a variable, cant be cached
                cacheable &= !isavar;
            }
        } else if (functor instanceof ASTIdentifierAccess) {
            // a method call on target
            methodName = ((ASTIdentifierAccess) functor).getName();
            symbol = -1;
            functor = null;
            cacheable = true;
        } else if (functor != null) {
            // ...(x)(y)
            symbol = -1 - 1; // -2;
            methodName = null;
            cacheable = false;
        } else if (!node.isSafeLhs(isSafe())) {
            return unsolvableMethod(node, "?(...)");
        } else {
            // safe lhs
            return null;
        }

        // solving the call site
        CallDispatcher call = new CallDispatcher(node, cacheable);
        try {
            // do we have a  cached version method/function name ?
            Object eval = call.tryEval(target, methodName, argv);
            if (JexlEngine.TRY_FAILED != eval) {
                return eval;
            }
            boolean functorp = false;
            boolean narrow = false;
            // pseudo loop to try acquiring methods without and with argument narrowing
            while (true) {
                call.narrow = narrow;
                // direct function or method call
                if (functor == null || functorp) {
                    // try a method or function from context
                    if (call.isTargetMethod(target, methodName, argv)) {
                        return call.eval(methodName);
                    }
                    if (target == context) {
                        // solve 'null' namespace
                        Object namespace = resolveNamespace(null, node);
                        if (namespace != null
                            && namespace != context
                            && call.isTargetMethod(namespace, methodName, argv)) {
                            return call.eval(methodName);
                        }
                        // do not try context function since this was attempted
                        // 10 lines above...; solve as an arithmetic function
                        if (call.isArithmeticMethod(methodName, argv)) {
                            return call.eval(methodName);
                        }
                        // could not find a method, try as a property of a non-context target (performed once)
                    } else {
                        // try prepending target to arguments and look for
                        // applicable method in context...
                        Object[] pargv = functionArguments(target, narrow, argv);
                        if (call.isContextMethod(methodName, pargv)) {
                            return call.eval(methodName);
                        }
                        // ...or arithmetic
                        if (call.isArithmeticMethod(methodName, pargv)) {
                            return call.eval(methodName);
                        }
                        // the method may also be a functor stored in a property of the target
                        if (!narrow) {
                            JexlPropertyGet get = uberspect.getPropertyGet(target, methodName);
                            if (get != null) {
                                functor = get.tryInvoke(target, methodName);
                                functorp = functor != null;
                            }
                        }
                    }
                }
                // this may happen without the above when we are chaining call like x(a)(b)
                // or when a var/symbol or antish var is used as a "function" name
                if (functor != null) {
                    // lambda, script or jexl method will do
                    if (functor instanceof JexlScript) {
                        return ((JexlScript) functor).execute(context, argv);
                    }
                    if (functor instanceof JexlMethod) {
                        return ((JexlMethod) functor).invoke(target, argv);
                    }
                    final String mCALL = "call";
                    // may be a generic callable, try a 'call' method
                    if (call.isTargetMethod(functor, mCALL, argv)) {
                        return call.eval(mCALL);
                    }
                    // functor is a var, may be method is a global one ?
                    if (isavar && target == context) {
                        if (call.isContextMethod(methodName, argv)) {
                            return call.eval(methodName);
                        }
                        if (call.isArithmeticMethod(methodName, argv)) {
                            return call.eval(methodName);
                        }
                    }
                    // try prepending functor to arguments and look for
                    // context or arithmetic function called 'call'
                    Object[] pargv = functionArguments(functor, narrow, argv);
                    if (call.isContextMethod(mCALL, pargv)) {
                        return call.eval(mCALL);
                    }
                    if (call.isArithmeticMethod(mCALL, pargv)) {
                        return call.eval(mCALL);
                    }
                }
                // if we did not find an exact method by name and we haven't tried yet,
                // attempt to narrow the parameters and if this succeeds, try again in next loop
                if (!narrow && arithmetic.narrowArguments(argv)) {
                    narrow = true;
                    // continue;
                } else {
                    break;
                }
            }
            // we have either evaluated and returned or no method was found
            return node.isSafeLhs(isSafe())
                    ? null
                    : unsolvableMethod(node, methodName, argv);
        } catch (JexlException.TryFailed xany) {
            throw invocationException(node, methodName, xany.getCause());
        } catch (JexlException xthru) {
            throw xthru;
        } catch (Exception xany) {
            throw invocationException(node, methodName, xany);
        }
    }

    @Override
    protected Object visit(ASTConstructorNode node, Object data) {
        if (isCancelled()) {
            throw new JexlException.Cancel(node);
        }
        // first child is class or class name
        final Object target = node.jjtGetChild(0).jjtAccept(this, data);
        // get the ctor args
        int argc = node.jjtGetNumChildren() - 1;
        Object[] argv = new Object[argc];
        for (int i = 0; i < argc; i++) {
            argv[i] = node.jjtGetChild(i + 1).jjtAccept(this, data);
        }

        try {
            boolean cacheable = cache;
            // attempt to reuse last funcall cached in volatile JexlNode.value
            if (cacheable) {
                Object cached = node.jjtGetValue();
                if (cached instanceof Funcall) {
                    Object eval = ((Funcall) cached).tryInvoke(this, null, target, argv);
                    if (JexlEngine.TRY_FAILED != eval) {
                        return eval;
                    }
                }
            }
            boolean narrow = false;
            JexlMethod ctor = null;
            Funcall funcall = null;
            while (true) {
                // try as stated
                ctor = uberspect.getConstructor(target, argv);
                if (ctor != null) {
                    if (cacheable && ctor.isCacheable()) {
                        funcall = new Funcall(ctor, narrow);
                    }
                    break;
                }
                // try with prepending context as first argument
                Object[] nargv = callArguments(context, narrow, argv);
                ctor = uberspect.getConstructor(target, nargv);
                if (ctor != null) {
                    if (cacheable && ctor.isCacheable()) {
                        funcall = new ContextualCtor(ctor, narrow);
                    }
                    argv = nargv;
                    break;
                }
                // if we did not find an exact method by name and we haven't tried yet,
                // attempt to narrow the parameters and if this succeeds, try again in next loop
                if (!narrow && arithmetic.narrowArguments(argv)) {
                    narrow = true;
                    continue;
                }
                // we are done trying
                break;
            }
            // we have either evaluated and returned or might have found a ctor
            if (ctor != null) {
                Object eval = ctor.invoke(target, argv);
                // cache executor in volatile JexlNode.value
                if (funcall != null) {
                    node.jjtSetValue(funcall);
                }
                return eval;
            }
            String tstr = target != null ? target.toString() : "?";
            return unsolvableMethod(node, tstr, argv);
        } catch (JexlException.Method xmethod) {
            throw xmethod;
        } catch (Exception xany) {
            String tstr = target != null ? target.toString() : "?";
            throw invocationException(node, tstr, xany);
        }
    }

    @Override
    protected Object visit(ASTJxltLiteral node, Object data) {
        TemplateEngine.TemplateExpression tp = (TemplateEngine.TemplateExpression) node.jjtGetValue();
        if (tp == null) {
            TemplateEngine jxlt = jexl.jxlt();
            tp = jxlt.parseExpression(node.jexlInfo(), node.getLiteral(), frame != null ? frame.getScope() : null);
            node.jjtSetValue(tp);
        }
        if (tp != null) {
            return tp.evaluate(frame, context);
        }
        return null;
    }

    @Override
    protected Object visit(ASTAnnotation node, Object data) {
        throw new UnsupportedOperationException(ASTAnnotation.class.getName() + ": Not supported.");
    }

    @Override
    protected Object visit(ASTAnnotatedStatement node, Object data) {
        return processAnnotation(node, 0, data);
    }

    /**
     * Processes an annotated statement.
     * @param stmt the statement
     * @param index the index of the current annotation being processed
     * @param data the contextual data
     * @return  the result of the statement block evaluation
     */
    protected Object processAnnotation(final ASTAnnotatedStatement stmt, final int index, final Object data) {
        // are we evaluating the block ?
        final int last = stmt.jjtGetNumChildren() - 1;
        if (index == last) {
            JexlNode cblock = stmt.jjtGetChild(last);
            // if the context has changed, might need a new interpreter
            final JexlArithmetic jexla = arithmetic.options(context);
            if (jexla != arithmetic) {
                if (!arithmetic.getClass().equals(jexla.getClass())) {
                    logger.warn("expected arithmetic to be " + arithmetic.getClass().getSimpleName()
                            + ", got " + jexla.getClass().getSimpleName()
                    );
                }
                Interpreter ii = new Interpreter(Interpreter.this, jexla);
                Object r = cblock.jjtAccept(ii, data);
                if (ii.isCancelled()) {
                    Interpreter.this.cancel();
                }
                return r;
            } else {
                return cblock.jjtAccept(Interpreter.this, data);
            }
        }
        // tracking whether we processed the annotation
        final boolean[] processed = new boolean[]{false};
        final Callable<Object> jstmt = new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                processed[0] = true;
                try {
                    return processAnnotation(stmt, index + 1, data);
                } catch(JexlException.Return xreturn) {
                    return xreturn;
                } catch(JexlException.Break xbreak) {
                    return xbreak;
                } catch(JexlException.Continue xcontinue) {
                    return xcontinue;
                }
            }
        };
        // the annotation node and name
        final ASTAnnotation anode = (ASTAnnotation) stmt.jjtGetChild(index);
        final String aname = anode.getName();
        // evaluate the arguments
        Object[] argv = anode.jjtGetNumChildren() > 0
                        ? visit((ASTArguments) anode.jjtGetChild(0), null) : null;
        // wrap the future, will recurse through annotation processor
        Object result;
        try {
            result = processAnnotation(aname, argv, jstmt);
            // not processing an annotation is an error
            if (!processed[0]) {
                return annotationError(anode, aname, null);
            }
        } catch (JexlException xany) {
            throw xany;
        } catch (Exception xany) {
            return annotationError(anode, aname, xany);
        }
        // the caller may return a return, break or continue
        if (result instanceof JexlException) {
            throw (JexlException) result;
        }
        return result;
    }

    /**
     * Delegates the annotation processing to the JexlContext if it is an AnnotationProcessor.
     * @param annotation    the annotation name
     * @param args          the annotation arguments
     * @param stmt          the statement / block that was annotated
     * @return the result of statement.call()
     * @throws Exception if anything goes wrong
     */
    protected Object processAnnotation(String annotation, Object[] args, Callable<Object> stmt) throws Exception {
        return context instanceof JexlContext.AnnotationProcessor
                ? ((JexlContext.AnnotationProcessor) context).processAnnotation(annotation, args, stmt)
                : stmt.call();
    }
}

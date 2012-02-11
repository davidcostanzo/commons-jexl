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
package org.apache.commons.jexl3.parser;

import org.apache.commons.jexl3.internal.Scope;

/**
 * Enhanced script to allow parameters declaration.
 */
public class ASTJexlLambda extends ASTJexlScript {
    public ASTJexlLambda(int id) {
        super(id);
    }

    public ASTJexlLambda(Parser p, int id) {
        super(p, id);
    }
        
    /**
     * Creates an array of arguments by copying values up to the number of parameters.
     * @param values the argument values
     * @return the arguments array
     */
    public Scope.Frame createFrame(Scope.Frame frame, Object... values) {
        if (scope != null) {
            Scope.Frame cframe = scope.createFrame(frame);
            if (cframe != null) {
                return cframe.assign(values);
            }
        }
        return null;
    }
}

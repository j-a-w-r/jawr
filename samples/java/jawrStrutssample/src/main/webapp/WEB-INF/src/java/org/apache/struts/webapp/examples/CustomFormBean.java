/*
 * $Id: CustomFormBean.java 471754 2006-11-06 14:55:09Z husted $
 *
 * Copyright 1999-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.struts.webapp.examples;

import org.apache.struts.config.FormBeanConfig;

/**
 * Custom FormBeanConfig to demonstrate usage.
 *
 * @version $Rev: 471754 $ $Date: 2006-11-06 08:55:09 -0600 (Mon, 06 Nov 2006) $
 */

public final class CustomFormBean extends FormBeanConfig {


    // --------------------------------------------------- Instance Variables


    /**
     * An example String property.
     */
    private String example = "";


    // ----------------------------------------------------------- Properties


    /**
     * Return the example String.
     */
    public String getExample() {

        return (this.example);

    }


    /**
     * Set the example String.
     *
     * @param example The new example String.
     */
    public void setExample(String example) {

        this.example = example;

    }

}

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
function textElementsNotEmpty(formName, elementNameArray){
    var obj;
    for(i in elementNameArray){
        var elem = elementNameArray[i];
        obj = eval("document." + formName + "." + elem); 
        if(isEmptyString(obj.value)){
            alert(elem + " must not be empty.");
            obj.focus(); 
            return false;             
        }
    }
    return true;
}
function isEmptyString(value){
    return value.length < 1;
}
function checkIntegral(formName, elementName){
    var obj = eval("document." + formName + "." + elementName); 
    if(isIntegral(obj.value)) return true;
    else{
        alert(elementName + " must be an integer.");
        obj.focus();
        return false;
    }
}

function isIntegral(value){
    var ints = "1234567890";
    for(i = 0; i < value.length; i++){
        if(ints.indexOf(value.charAt(i)) < 0) return false;
    }
    return true;
}
    

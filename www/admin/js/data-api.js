/*
 * Copyright 2017 Grzegorz Skorupa <g.skorupa at gmail.com>.
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

// get data from the service
function getData(url, query, token, callback, eventBus, successEventName, errorEventName, debug, appEventBus) {

    var oReq = new XMLHttpRequest();
    var defaultErrorEventName = "dataerror:";
    oReq.onerror = function (oEvent) {
        if (debug) {
            console.log("onerror " + this.status + " " + oEvent.toString())
        }
        ;
    };
    oReq.onload = function (oEvent) {/*getdataCallback(elementId, statusId);*/
        if (debug) {
            console.log("onload " + this.status + " " + oEvent.toString())
        }
        ;
        if (this.status != 200) {
            var fullErrName
            if (errorEventName == null) {
                fullErrName = defaultErrorEventName + this.status;
            } else {
                fullErrName = errorEventName;
            }
            if (appEventBus == null) {
                eventBus.trigger(fullErrName);
            } else {
                appEventBus.trigger(fullErrName);
            }
        }
        ;
    };
    oReq.onreadystatechange = function () {
        if (this.readyState == 4 && this.status == 200) {
            console.log(JSON.parse(this.responseText));
            if (callback != null) {
                callback(this.responseText, successEventName);
            } else {
                eventBus.trigger(successEventName);
            }
        } else if (this.readyState == 4 && this.status == 0) {
            //eventBus.trigger(errorEventName);
            if (errorEventName == null) {
                eventBus.trigger(defaultErrorEventName + this.status);
            } else {
                eventBus.trigger(errorEventName);
            }
        }
    };

    oReq.open("get", url, true);
    if (token != null) {
        oReq.withCredentials = true;
        oReq.setRequestHeader("Authentication", token);
    }
    oReq.send(query);
    return false;
}

function sendFormData(formData, method, url, token, callback, eventBus, successEventName, errorEventName, debug, appEventBus) {
    if (debug) {
        console.log("sendFormData ...")
    }
    ;
    var oReq = new XMLHttpRequest();
    var defaultErrorEventName = "dataerror:";
    oReq.onerror = function (oEvent) {
        if (debug) {
            console.log("onerror " + this.status + " " + oEvent.toString())
        }
        ;
    };
    oReq.onload = function (oEvent) {/*getdataCallback(elementId, statusId);*/
        if (debug) {
            console.log("onload " + this.status + " " + oEvent.toString())
        }
        ;
        if (this.status != 200) {
            var fullErrName
            if (errorEventName == null) {
                fullErrName = defaultErrorEventName + this.status;
            } else {
                fullErrName = errorEventName;
            }
            if (appEventBus == null) {
                eventBus.trigger(fullErrName);
            } else {
                appEventBus.trigger(fullErrName);
            }
        }
        ;
    };

    oReq.onreadystatechange = function () {
        if (this.readyState == 4 && (this.status == 200 || this.status == 201)) {
            console.log(JSON.parse(this.responseText));
            if (callback != null) {
                callback(this.responseText);
            } else {
                eventBus.trigger(successEventName);
            }
        } else if (this.readyState == 4 && this.status == 0) {
            //eventBus.trigger(errorEventName);
            if (errorEventName == null) {
                eventBus.trigger(defaultErrorEventName + this.status);
            } else {
                eventBus.trigger(errorEventName);
            }
        }
    };
    // method declared in the form is ignored
    oReq.open(method, url);
    if (token != null) {
        oReq.withCredentials = true;
        oReq.setRequestHeader("Authentication", token);
    }
    //oReq.send(new FormData(oFormElement));
    oReq.send(formData);
    return false;
}

function sendData(data, method, url, token, callback, eventBus, successEventName, errorEventName, debug, appEventBus) {
    if (debug) {
        console.log("sendData ...")
    }
    ;
    var urlEncodedData = "";
    var urlEncodedDataPairs = [];
    var name;
    var oReq = new XMLHttpRequest();
    var defaultErrorEventName = "dataerror:";

    // Turn the data object into an array of URL-encoded key/value pairs.
    for (name in data) {
        urlEncodedDataPairs.push(encodeURIComponent(name) + '=' + encodeURIComponent(data[name]));
    }

    // Combine the pairs into a single string and replace all %-encoded spaces to 
    // the '+' character; matches the behaviour of browser form submissions.
    urlEncodedData = urlEncodedDataPairs.join('&').replace(/%20/g, '+');

    oReq.onerror = function (oEvent) {
        if (debug) {
            console.log("onerror " + this.status + " " + oEvent.toString())
        }
        callback(oEvent.toString());
    };
    oReq.onload = function (oEvent) {/*getdataCallback(elementId, statusId);*/
        if (debug) {
            console.log("onload " + this.status + " " + oEvent.toString())
        }
        ;

        if (this.status < 200 || this.status > 201) {
            if (eventBus == null && appEventBus == null) {
                if (callback != null) {
                    callback('error:' + this.status)
                }
            } else {
                var fullErrName
                if (errorEventName == null) {
                    fullErrName = defaultErrorEventName + this.status;
                } else {
                    fullErrName = errorEventName;
                }
                if (appEventBus == null) {
                    eventBus.trigger(fullErrName);
                } else {
                    appEventBus.trigger(fullErrName);
                }
            }
        }
        ;
    };

    oReq.onreadystatechange = function () {
        if (this.readyState == 4 && (this.status == 200 || this.status == 201)) {
            console.log(JSON.parse(this.responseText));
            if (callback != null) {
                callback(this.responseText);
            } else {
                eventBus.trigger(successEventName);
            }
        } else if (this.readyState == 4 && this.status == 0) {
            if(debug){ console.log("onreadystatechange") }
            if (callback != null) {
                callback('error:' + this.status + ' status:'+this.readyState)
            } else {
                if (errorEventName == null) {
                    eventBus.trigger(defaultErrorEventName + this.status);
                } else {
                    eventBus.trigger(errorEventName);
                }
            }
        } else {
        //console.log('XXXXXXXX '+this.status);
        }
    };

    // method declared in the form is ignored
    oReq.open(method, url);
    oReq.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');
    if (token != null) {
        oReq.withCredentials = true;
        oReq.setRequestHeader("Authentication", token);
    }
    oReq.send(urlEncodedData);
    return false;
}

function deleteData(url, token, callback, eventBus, successEventName, errorEventName, debug, appEventBus) {

    var oReq = new XMLHttpRequest();
    var defaultErrorEventName = "dataerror:";
    oReq.onerror = function (oEvent) {
        if (debug) {
            console.log("onerror " + this.status + " " + oEvent.toString())
        }
        ;
    };
    oReq.onload = function (oEvent) {/*getdataCallback(elementId, statusId);*/
        if (debug) {
            console.log("onload " + this.status + " " + oEvent.toString())
        }
        ;
        if (this.status != 200) {
            var fullErrName
            if (errorEventName == null) {
                fullErrName = defaultErrorEventName + this.status;
            } else {
                fullErrName = errorEventName;
            }
            if (appEventBus == null) {
                eventBus.trigger(fullErrName);
            } else {
                appEventBus.trigger(fullErrName);
            }
        }
        ;
    };
    oReq.onreadystatechange = function () {
        if (this.readyState == 4 && this.status == 200) {
            console.log(JSON.parse(this.responseText));
            if (callback != null) {
                callback(this.responseText);
            } else {
                eventBus.trigger(successEventName);
            }
        } else if (this.readyState == 4 && this.status == 0) {
            //eventBus.trigger(errorEventName);
            if (errorEventName == null) {
                eventBus.trigger(defaultErrorEventName + this.status);
            } else {
                eventBus.trigger(errorEventName);
            }
        }
    };

    oReq.open("DELETE", url, true);
    if (token != null) {
        oReq.withCredentials = true;
        oReq.setRequestHeader("Authentication", token);
    }
    oReq.send(null);
    return false;
}

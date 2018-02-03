/* Copyright 2017 Grzegorz Skorupa <g.skorupa at gmail.com>.
 * Licensed under the Apache License, Version 2.0 
 */

function getData(url, query, token, callback, eventListener, errorEventName) {

    var oReq = new XMLHttpRequest();
    var defaultErrorEventName = "err:";
    oReq.onerror = function (oEvent) {
        app.log("onerror " + this.status + " " + oEvent.toString())
        eventListener.trigger("auth"+this.status);
    }
    oReq.onreadystatechange = function () {
        if (this.readyState == 4) {
            if (this.status == 200) {
                app.log(JSON.parse(this.responseText));
                callback(this.responseText);
            } else {
                var tmpErrName
                if (errorEventName == null) {
                    tmpErrName=defaultErrorEventName + this.status
                } else {
                    tmpErrName=errorEventName
                }
                eventListener.trigger(tmpErrName);
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

function sendData(data, method, url, token, callback, eventListener, errorEventName) {
    app.log("sendData ...")
    var urlEncodedData = "";
    var urlEncodedDataPairs = [];
    var name;
    var oReq = new XMLHttpRequest();
    var defaultErrorEventName = "err:";
    for (name in data) {
        urlEncodedDataPairs.push(encodeURIComponent(name) + '=' + encodeURIComponent(data[name]));
    }
    urlEncodedData = urlEncodedDataPairs.join('&').replace(/%20/g, '+');
    oReq.onerror = function (oEvent) {
        app.log("onerror " + this.status + " " + oEvent.toString())
        callback(oEvent.toString());
    }
    oReq.onreadystatechange = function () {
        if (this.readyState == 4) {
            if (this.status > 199 && this.status < 203) {
                app.log(JSON.parse(this.responseText));
                callback(this.responseText);
            } else {
                app.log("onreadystatechange")
                if (errorEventName == null) {
                    eventListener.trigger(defaultErrorEventName + this.status);
                } else {
                    eventListener.trigger(errorEventName);
                }
            }
        }
    }
    oReq.open(method, url);
    oReq.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');
    if (token != null) {
        oReq.withCredentials = true;
        oReq.setRequestHeader("Authentication", token);
    }
    oReq.send(urlEncodedData);
    return false;
}

function sendFormData(formData, method, url, token, callback, eventBus, successEventName, errorEventName, debug, appEventBus) {
    if (debug) {
        console.log("sendFormData ...")
    };
    var oReq = new XMLHttpRequest();
    var defaultErrorEventName = "dataerror:";
    oReq.onerror = function (oEvent) {
        if (debug) {
            console.log("onerror " + this.status + " " + oEvent.toString())
        };
    };
    oReq.onload = function (oEvent) {
        if (debug) {
            console.log("onload " + this.status + " " + oEvent.toString())
        };
        if (this.status != 200) {
        if (eventBus == null && appEventBus == null) {
            if (callback != null) {
                callback('error:' + this.status)
            }
        }else{
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
        };
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
            if (errorEventName == null) {
                eventBus.trigger(defaultErrorEventName + this.status);
            } else {
                eventBus.trigger(errorEventName);
            }
        }
    };
    oReq.open(method, url);
    if (token != null) {
        oReq.withCredentials = true;
        oReq.setRequestHeader("Authentication", token);
    }
    oReq.send(formData);
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
    oReq.onload = function (oEvent) {
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
        };
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

function afterLogin(newKey, name) {
    var key = newKey.trim();
    if (key == "") {
        app.log('Login failed');
    } else {
        app.offline = false;
        app.user.token = key;
        app.user.name = name;
        app.user.status = "logged-in";
    }
}

function loginSubmit(oFormElement, eventBus, successEventName, errorEventName) {
    var login;
    var password;
    var oField = "";
    var sEncoded;
    for (var nItem = 0; nItem < oFormElement.elements.length; nItem++) {
        oField = oFormElement.elements[nItem];
        if (!oField.hasAttribute("name")) {
            continue;
        }
        if (oField.name === "login") {
            login = oField.value;
        } else if (oField.name === "password") {
            password = oField.value;
        }
    }
    var oReq = new XMLHttpRequest();
    oReq.onerror = function (oEvent) {
        app.log(oEvent.toString());
        app.user.status = "logged-out";
        eventBus.trigger(errorEventName);
    };
    oReq.onload = function (oEvent) {
        app.log(oEvent.toString());
    };
    oReq.onreadystatechange = function () {
        if (this.readyState == 4 && this.status == 200) {
            afterLogin(oReq.responseText, login);
            eventBus.trigger(successEventName);
        } else if (this.readyState == 4 && this.status > 400) {
            app.user.status = "logged-out";
            eventBus.trigger(errorEventName);
            app.log("result code == "+this.status);
        }
    };
    sEncoded = utoa(login + ":" + password);
    oReq.open("post", app.authAPI);
    app.log('app.authAPI='+app.authAPI)
    oReq.withCredentials = true;
    oReq.setRequestHeader("Authentication", "Basic " + sEncoded);
    oReq.setRequestHeader("Accept", "text/plain");
    oReq.send("action=login");
    return false;
}
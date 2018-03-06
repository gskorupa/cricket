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

var globalEvents = riot.observable();

var app = {
    "myData": {"todos": []},
    "user": {
        "name": "",
        "token": "",
        "status": "logged-out",
        "alerts": []
    },
    "offline": false,
    "authAPI": "http://localhost:8080/api/auth",
    "csAPI": "http://localhost:8080/api/cs",
    "cmAPI": "http://localhost:8080/api/cm",
    "userAPI": "http://localhost:8080/api/user",
    "currentPage": "main",
    "language": "en",
    "languages": ["en", "pl", "fr"],
    "debug": false,
    "localUid": 0,
    "requests": 0,
    "log": function(message){if(app.debug){console.log(message)}}
}

// ucs-2 string to base64 encoded ascii
    function utoa(str) {
        return window.btoa(unescape(encodeURIComponent(str)));
    }
    // base64 encoded ascii to ucs-2 string
    function atou(str) {
        return decodeURIComponent(escape(window.atob(str)));
    }
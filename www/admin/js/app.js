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
        "alerts": [],
        "dashboardID": '',
        "dashboards": []
    },
    "offline": false,
    "authAPI": "http://signode.unicloud.pl/api/auth",
    "csAPI": "http://signode.unicloud.pl/api/cs",
    "cmAPI": "http://signode.unicloud.pl/api/cm",
    "userAPI": "http://signode.unicloud.pl/api/user",
    "currentPage": "main",
    "language": "en",
    "languages": ["en", "pl", "fr"],
    "debug": false,
    "localUid": 0,
    "dconf": {"widgets":[]}, // configurations of user's widgets on the dashboard page
     //   {},{},{},{},{},{},{},{},{},{},{},{}
    //],
    "widgets": [ // widgets on the dashboard page - hardcoded structure
        [{}, {}, {}, {}],
        [{}, {}, {}, {}],
        [{}, {}, {}, {}]
    ],
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
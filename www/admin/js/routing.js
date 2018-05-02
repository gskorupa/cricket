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

route(function(id){
    switch (id){
        case "login":
            app.currentPage = "login";
            //globalEvents.trigger('pageselected:login');
            break;
        case "logout":
            app.currentPage = "logout";
            //globalEvents.trigger('pageselected:logout');
            break;
        case "documents":
            if(app.user.role.includes('redactor')){
                app.currentPage = "documents";
                //globalEvents.trigger('pageselected:documents');
            }else{
                app.currentPage = "main";
            }
            break;
        case "tags":
            if(app.user.role.includes('redactor')){
                app.currentPage = "tags";
            }else{
                app.currentPage = "main";
            }
            //globalEvents.trigger('pageselected:tags');
            break;
        case "users":
            if(app.user.role.includes('admin')){
                app.currentPage = "users";
            }else{
                app.currentPage = "main";
            }
            //globalEvents.trigger('pageselected:users');
            break;
        case "":
            app.currentPage = "main";
            globalEvents.trigger('pageselected:main');
            break;
        case "main":
            app.currentPage = "main";
            //globalEvents.trigger('pageselected:main');
            break;
        case "pl":
            app.language = 'pl'
            riot.mount('raw')
            riot.update()
            break;
        case "en":
            app.language = 'en'
            riot.mount('raw')
            riot.update()
            break;
        case "fr":
            app.language = 'fr'
            riot.mount('raw')
            riot.update()
            break;
        default:
            app.log('DEFAULT ROUTE')
            break;
    }
    riot.update();
})


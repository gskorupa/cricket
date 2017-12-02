<user_form>
    <div class="panel panel-primary">
        <div class="panel-heading module-title" if={ self.mode == 'create' }><h2>{ labels.user_new[app.language] }</h2></div>
        <div class="panel-heading module-title" if={ self.mode == 'update' }><h2>{ labels.user_modify[app.language] }</h2></div>
        <div class="panel-heading module-title" if={ self.mode == 'view' }><h2>{ labels.user_view[app.language] }</h2></div>
        <div class="panel-body">
            <form onsubmit={ self.submitForm }>
                <div class="form-group">
                    <label for="uid">{ labels.uid[app.language] }</label>
                    <input class="form-control" id="uid" name="uid" type="text" value={ user.uid } readonly={ self.mode != 'create' } required>
                </div>
                <div class="form-group">
                    <label for="email">{ labels.email[app.language] }</label>
                    <input class="form-control" id="email" name="email" type="text" value={ user.email } readonly={ !allowEdit } required>
                </div>
                <div class="form-group" if={ adminMode }>
                    <label for="type">{ labels.type[app.language] }</label>
                    <input class="form-control" id="type" name="type" type="text" value={ userTypeAsString(user.type) } readonly={ self.mode != 'create' } required>
                </div>
                <div class="form-group" if={ adminMode }>
                    <label for="role">{ labels.role[app.language] }</label>
                    <input class="form-control" id="role" name="role" type="text"  value={ user.role } readonly={ !allowEdit }>
                </div>
                <div class="form-group">
                    <label for="confirmString">{ labels.confirmString[app.language] }</label>
                    <input class="form-control" id="confirmString" name="confirmString" type="text" value={ user.confirmString } readonly={ true }>
                </div>
                <div class="form-group">
                    <label for="confirmed">{ labels.confirmed[app.language] }</label>
                    <input class="form-control" id="confirmed" name="confirmed" type="text" value={ user.confirmed } readonly={ !allowEdit || !adminMode } required>
                </div>
                <div class="form-group">
                    <label for="generalNotificationChannel">{ labels.generalNotifications[app.language] }</label>
                    <input class="form-control" id="generalNotificationChannel" name="generalNotificationChannel" type="text" value={ user.generalNotificationChannel } readonly={ !allowEdit }>
                </div>
                <div class="form-group">
                    <label for="infoNotificationChannel">{ labels.infoNotifications[app.language] }</label>
                    <input class="form-control" id="infoNotificationChannel" name="infoNotificationChannel" type="text" value={ user.infoNotificationChannel } readonly={ !allowEdit }>
                </div>
                <div class="form-group">
                    <label for="warningNotificationChannel">{ labels.warningNotifications[app.language] }</label>
                    <input class="form-control" id="warningNotificationChannel" name="warningNotificationChannel" type="text" value={ user.warningNotificationChannel } readonly={ !allowEdit }>
                </div>
                <div class="form-group">
                    <label for="alertNotificationChannel">{ labels.alertNotifications[app.language] }</label>
                    <input class="form-control" id="alertNotificationChannel" name="alertNotificationChannel" type="text" value={ user.alertNotificationChannel } readonly={ !allowEdit }>
                </div>
                <div class="form-group">
                    <label for="unregisterRequested">{ labels.unregisterRequested[app.language] }</label>
                    <input class="form-control" id="unregisterRequested" name="unregisterRequested" type="text" value={ user.unregisterRequested } readonly={ true } required>
                </div>
                <button type="button" onclick={ close } class="btn btn-default">{ labels.cancel[app.language] }</button>
                <span>&nbsp;</span>
                <button type="submit" class="btn btn-default  pull-right" disabled={ !allowEdit }>{ labels.save[app.language] }</button>
            </form>
        </div>
    </div>
    <script>
        this.visible = true
                self = this
                self.listener = riot.observable()
                self.callbackListener
                self.allowEdit = false
                self.adminMode = false
                self.method = 'POST'
                self.mode = 'view'
                self.user = {
                'uid': '',
                        'email': '',
                        'type': '',
                        'role': '',
                        'confirmString': '',
                        'confirmed': false,
                        'password': '',
                        'generalNotificationChannel': '',
                        'infoNotificationChannel': '',
                        'warningNotificationChannel': '',
                        'alertNotificationChannel': '',
                        'unregisterRequested': false
                }

        globalEvents.on('data:submitted', function(event){
        if (app.debug) { console.log("I'm happy!") }
        });
        init(eventListener, uid, editable, isAdmin){
        self.callbackListener = eventListener
                self.allowEdit = editable
                self.adminMode = isAdmin
                self.method = 'POST'
                app.log('HELLO ' + uid)
                app.log('CALLBACK: ' + self.callbackListener)
                app.log('EDITABLE: ' + self.allowEdit)
                if (uid != 'NEW'){
        readUser(uid)
                self.method = 'PUT'
                if (self.allowEdit){
        self.mode = 'update'
        } else{
        self.mode = 'view'
        }
        } else{
        self.mode = 'create'
        }
        }

        userTypeAsString(type){
        switch (type){
        case 2:
                return 'APPLICATION'
                case 1:
                return 'OWNER'
                default:
                return 'USER'
        }
        }

        self.submitForm = function(e){
            console.log('SUBMITFORM')
            e.preventDefault()

            var formData = { }
            if (e.target.elements['uid'].value) {
                formData.uid = e.target.elements['uid'].value
            }
            formData.email = e.target.elements['email'].value
            if (self.adminMode){
                formData.type = e.target.elements['type'].value
                if (e.target.elements['role'].value != '') {
                    formData.role = e.target.elements['role'].value
                }
            }
            if (e.target.elements['generalNotificationChannel'].value != '') {formData.generalNotifications = e.target.elements['generalNotificationChannel'].value}
            if (e.target.elements['infoNotificationChannel'].value != '') {formData.infoNotifications = e.target.elements['infoNotificationChannel'].value}
            if (e.target.elements['warningNotificationChannel'].value != '') {formData.warningNotifications = e.target.elements['warningNotificationChannel'].value}
            if (e.target.elements['alertNotificationChannel'].value != '') {formData.alertNotifications = e.target.elements['alertNotificationChannel'].value}
            if (e.target.elements['confirmString'].value != '') {formData.confirmString = e.target.elements['confirmString'].value}
            if (e.target.elements['confirmed'].value != '') {formData.confirmed = e.target.elements['confirmed'].value}
            if (self.mode == 'create') {
                formData.password = generatePassword()
            }
            if (e.target.elements['unregisterRequested'].value != '') {
                formData.unregisterRequested = e.target.elements['unregisterRequested'].value
            }

            app.log(JSON.stringify(formData))
            urlPath = ''
            if (self.method == 'PUT'){
                urlPath = '/' + formData.uid
            }
            sendData(
                formData,
                self.method,
                app.userAPI + urlPath,
                app.user.token,
                self.close,
                null, //self.listener, 
                'submit:OK',
                'submit:ERROR',
                app.debug,
                globalEvents
                )
                //self.callbackListener.trigger('submitted')
        }

        self.close = function(object){
            var text = '' + object
            console.log('CALBACK: ' + object)
            if (text.startsWith('"') || text.startsWith('{')){
                self.callbackListener.trigger('submitted')
            } else if (text.startsWith('error:202')){
                self.callbackListener.trigger('submitted')
            } else if (text.startsWith('[object MouseEvent')){
                self.callbackListener.trigger('cancelled')
            } else{
                alert(text)
            }
        }

        var update = function (text) {
            app.log("USER: " + text)
            self.user = JSON.parse(text);
            riot.update();
        }

        var readUser = function (uid) {
        getData(app.userAPI + '/' + uid,
                null,
                app.user.token,
                update,
                self.listener, //globalEvents
                'OK',
                null, // in case of error send response code
                app.debug,
                globalEvents
                );
        }

        var generatePassword = function(){
        return window.btoa((new Date().getMilliseconds() + ''))
        }

        this.labels = {
        "user_view": {
        "en": "User Preview",
                "pl": "Podgląd Danych Użytkownika"
        },
                "user_new": {
                "en": "New User",
                        "pl": "Nowe Konto Użytkownika"
                },
                "user_modify": {
                "en": "Modify User",
                        "pl": "Zmiana Danych Użytkownika"
                },
                "email": {
                "en": "* e-mail address",
                        "pl": "* adres e-mail"
                },
                "generalNotifications": {
                "en": "Channel Definition For Notification Type: GENERAL",
                        "pl": "Definicja Kanału Dla Notyfikacji Typu: GENERAL"
                },
                "infoNotifications": {
                "en": "Channel Definition For Notification Type: INFO",
                        "pl": "Definicja Kanału Dla Notyfikacji Typu: INFO"
                },
                "warningNotifications": {
                "en": "Channel Definition For Notification Type: WARNING",
                        "pl": "Definicja Kanału Dla Notyfikacji Typu: WARNING"
                },
                "alertNotifications": {
                "en": "Channel Definition For Notification Type: ALERT",
                        "pl": "Definicja Kanału Dla Notyfikacji Typu: ALERT"
                },
                "uid": {
                "en": "* UID",
                        "pl": "* UID"
                },
                "type": {
                "en": "* User Type",
                        "pl": "* Typ Użytkownika"
                },
                "role": {
                "en": "User Roles",
                        "pl": "Role Użytkownika"
                },
                "password": {
                "en": "Password",
                        "pl": "Hasło"
                },
                "confirmString": {
                "en": "Confirmation Code",
                        "pl": "Kod Potwierdzenia"
                },
                "confirmed": {
                "en": "Is Confirmed?",
                        "pl": "Czy potwierdzone?"
                }
        ,
                "unregisterRequested": {
                "en": "* Unregistration Requested?",
                        "pl": "* Żądanie usunięcia konta?"
                },
                "save": {
                "en": "Save",
                        "pl": "Zapisz"
                },
                "cancel": {
                "en": "Cancel",
                        "pl": "Porzuć"
                }
        }
    </script>
</user_form>

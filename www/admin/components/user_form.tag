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
                    <input class="form-control" id="email" name="email" type="email" value={ user.email } readonly={ !allowEdit } required>
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
                <div class="form-check">
                    <input class="form-check-input" type="checkbox" value="true" id="confirmed" disabled={ !allowEdit || !adminMode } checked={ user.confirmed }>
                    <label class="form-check-label" for="confirmed">{ labels.confirmed[app.language] }</label>
                </div>
                <div class="form-check">
                    <input class="form-check-input" type="checkbox" value="true" id="unregisterRequested" disabled={ true } checked={ user.unregisterRequested }>
                    <label class="form-check-label" for="unregisterRequested">{ labels.unregisterRequested[app.language] }</label>
                </div>
                <div class="form-group">
                    <label>{ labels.number[app.language] } {user.number}</label>
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
            'unregisterRequested': false
        }

        globalEvents.on('data:submitted', function(event){
            //
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
                case 1:
                    return 'OWNER'
                default:
                    return 'USER'
            }
        }
        
        getConfirmedState(){
            if(self.user.confirmed){
                return 'checked'
            }else{
                return ''
            }
        }

        self.submitForm = function(e){
            app.log('SUBMITFORM')
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
            if (e.target.elements['confirmString'].value != '') {formData.confirmString = e.target.elements['confirmString'].value}
            if (e.target.elements['confirmed'].checked) {formData.confirmed = 'true'}else{formData.confirmed = 'false'}
            if (self.mode == 'create') {
                formData.password = generatePassword()
            }
            if (e.target.elements['unregisterRequested'].checked) {formData.unregisterRequested = e.target.elements['unregisterRequested'].value}

            app.log(JSON.stringify(formData))
            urlPath = ''
            if (self.method == 'PUT'){
                urlPath = '/' + formData.uid
            }
            sendData(formData,self.method,app.userAPI + urlPath,app.user.token,self.close,globalEvents)
        }

        self.close = function(object){
            var text = '' + object
            app.log('CALLBACK: ' + object)
            if (text.startsWith('"') || text.startsWith('{')){
                self.callbackListener.trigger('submitted')
            } else if (text.startsWith('error:202')){
                self.callbackListener.trigger('submitted')
            } else if (text.startsWith('[object MouseEvent')){
                self.callbackListener.trigger('cancelled')
            } else if (text.startsWith('error:409')){
                alert('This login is already registered!')
            } else{
                alert(text)
            }
        }

        var update = function (text) {
            app.log("USER: " + text)
            self.user = JSON.parse(text);
            riot.update();
        }
        self.listener.on('*', function(event){
          riot.update()
        })

        var readUser = function (uid) {
            getData(app.userAPI+'/'+uid,null,app.user.token,update,self.listener)
        }

        var generatePassword = function(){
            return window.btoa((new Date().getMilliseconds() + ''))
        }

        this.labels = {
            "user_view": {
                "en": "User Preview",
                "fr": "User Preview",
                "pl": "Podgląd Danych Użytkownika"
            },
            "user_new": {
                "en": "New User",
                "fr": "New User",
                "pl": "Nowe Konto Użytkownika"
            },
            "user_modify": {
                "en": "Modify User",
                "fr": "Modify User",
                "pl": "Zmiana Danych Użytkownika"
            },
            "email": {
                "en": "* e-mail address",
                "fr": "* e-mail address",
                "pl": "* adres e-mail"
            },
            "uid": {
                "en": "* Login (UID)",
                "fr": "* Login (UID)",
                "pl": "* Login (UID)"
            },
            "type": {
                "en": "* User Type",
                "fr": "* User Type",
                "pl": "* Typ Użytkownika"
            },
            "role": {
                "en": "User Roles",
                "fr": "User Roles",
                "pl": "Role Użytkownika"
            },
            "password": {
                "en": "Password",
                "fr": "Password",
                "pl": "Hasło"
            },
            "confirmString": {
                "en": "Confirmation Code",
                "fr": "Confirmation Code",
                "pl": "Kod Potwierdzenia"
            },
            "confirmed": {
                "en": "Is Confirmed?",
                "fr": "Is Confirmed?",
                "pl": "Czy potwierdzone?"
            },
            "unregisterRequested": {
                "en": "* Unregistration Requested?",
                "fr": "* Unregistration Requested?",
                "pl": "* Żądanie usunięcia konta?"
            },
            "number": {
                "en": "no.",
                "fr": "no.",
                "pl": "nr"
            },
            "save": {
                "en": "Save",
                "fr": "Save",
                "pl": "Zapisz"
            },
            "cancel": {
                "en": "Cancel",
                "fr": "Cancel",
                "pl": "Porzuć"
            }
        }
    </script>
</user_form>

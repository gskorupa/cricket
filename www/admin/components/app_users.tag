<app_users>
    <div class="row" if={ selected }>
         <div class="col-md-12">
            <user_form ref="user_edit"></user_form>
        </div>
    </div>
    <div class="row" if={ selectedPwd }>
         <div class="col-md-12">
            <app_password_form ref="user_password"></app_password_form>
        </div>
    </div>
    <div class="row" if={ !selected && !selectedPwd }>
        <div class="col-md-12">
            <h2>{labels.title[app.language]} 
                <i class="material-icons clickable" onclick={ refreshUsers() }>refresh</i>
                <i class="material-icons clickable" onclick={ editUser('NEW', true) }>add</i>
            </h2>
            <div class="table-responsive-sm">
                <table id="doclist" class="table table-sm">
                    <thead>
                        <tr>
                            <th>{labels.t_no[app.language]}</th>
                            <th>{labels.t_uid[app.language]}</th>
                            <th>{labels.t_type[app.language]}</th>
                            <th>{labels.t_role[app.language]}</th>
                            <th>{labels.t_status[app.language]}</th>
                            <th class="text-right">
                                <i class="material-icons clickable" onclick={ editUser('NEW', true) }>add</i>
                            </th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr each={user in users}>
                            <td>{ user.number }</td>
                            <td>{ user.uid }</td>
                            <td>{ user.type }</td>
                            <td>{ user.role }</td>
                            <td>{ user.authStatus }</td>
                            <td class="text-right">
                                <i class="material-icons clickable" onclick={ editPassword(user.uid) } if={ user.uid == app.user.name }>vpn_key</i>
                                <i class="material-icons clickable" onclick={ editUser(user.uid, false) }>open_in_browser</i>
                                <i class="material-icons clickable" onclick={ editUser(user.uid, true) }>mode_edit</i>
                                <i class="material-icons clickable" onclick={ select(user.uid) } data-toggle="modal" data-target="#removeDialog">delete</i>
                            </td>
                        </tr>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
    <div class="row" >
        <div class="col-md-12">
            <div id="removeDialog" class="modal fade">
                <div class="modal-dialog modal-sm">
                    <div class="modal-content">
                        <div class="modal-header">
                            <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
                            <h4 class="modal-title">{labels.remove_title[app.language]}</h4>
                        </div>
                        <div class="modal-body">
                            <p>{labels.remove_question[app.language]}</p>
                            <p class="text-warning"><small>{labels.remove_info[app.language]}</small></p>
                        </div>
                        <div class="modal-footer">
                            <button type="button" class="btn btn-default" data-dismiss="modal" onclick={ select('') }>{labels.cancel[app.language]}</button>
                            <button type="button" class="btn btn-primary" data-dismiss="modal" onclick={ removeAccount() }>{labels.remove[app.language]}</button>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
    <script charset="UTF-8">
        var self = this
        self.listener = riot.observable()
        self.users = []
        self.selected = ''
        self.selectedPwd = ''
        self.removing = ''
        self.selectedLanguage = 'EN'

        this.on('mount', function(){
        self.selected = ''
        self.selectedPwd = ''
        readUserList()
        })

        self.listener.on('*', function (eventName) {
            app.log('LISTENER: ' + eventName)
            switch (eventName){
                case 'submitted':
                    self.selected = ''
                    readUserList()
                    break
                case 'pSubmitted':
                case 'passCancelled':
                    self.selectedPwd = ''
                    break
                case 'cancelled':
                    self.selected = ''
                    break
                case 'err:403':
                    globalEvents.trigger(eventName)
                    break
                default:
                    app.log('USERS: ' + eventName)
            }
            riot.update()
        })
        var readUserList = function () {
        app.log('reading docs ...')
        getData(app.userAPI,null,app.user.token,updateList,self.listener)
        }

        var updateList = function (text) {
        app.log("DOCUMENTS: " + text)
        self.users = JSON.parse(text)
        riot.update()
        }

        editUser(uid, allowEdit){
        return function(e){
        e.preventDefault()
        self.selected = uid
        riot.update()
        app.log('SELECTED FOR EDITING: ' + uid)
        self.refs.user_edit.init(self.listener, uid, allowEdit, true)
        }
        }

        editPassword(uid){
        return function(e){
        e.preventDefault()
        self.selectedPwd = uid
        riot.update()
        app.log('SELECTED FOR PASSWORD: ' + uid)
        self.refs.user_password.init(self.listener, uid)
        }
        }

        select(uid){
        return function(e){
        e.preventDefault()
        self.removing = uid
        riot.update()
        app.log('DEL SELECTED ' + uid)
        }
        }

        refreshUsers(){
        return function(e){
        app.log('refreshing...')
        readUserList()
        }
        }

        removeAccount(){
        return function(e){
        e.preventDefault()
        app.log('REMOVING ' + self.removing + ' ...')
        if (self.removing == app.user.name){
        alert('Operation not permitted')
        self.removing = ''
        } else{
        deleteData(app.userAPI+'/'+self.removing,app.user.token,self.closeRemove,globalEvents)
        }
        }
        }

        self.closeRemove = function(object){
        var text = '' + object
        app.log('CALBACK: ' + object)
        if (text.startsWith('{')){
        //
        } else if (text.startsWith('error:')){
        //it should'n happen
        //alert(text)
        }
        self.removing = ''
        readUserList()
        }

        self.labels = {
        "title": {
        "en": "users",
        "fr": "users",
        "pl": "użytkownicy"
        },
        "t_no": {
        "en": "NO.",
        "fr": "NO.",
        "pl": "NR"
        },
        "t_uid": {
        "en": "LOGIN",
        "fr": "LOGIN",
        "pl": "LOGIN"
        },
        "t_type": {
        "en": "TYPE",
        "fr": "TYPE",
        "pl": "TYP"
        },
        "t_role": {
        "en": "ROLE",
        "fr": "ROLE",
        "pl": "ROLA"
        },
        "t_status": {
        "en": "STATUS",
        "fr": "STATUS",
        "pl": "STATUS"
        },
        "remove": {
        "en": "Remove",
        "fr": "Remove",
        "pl": "Usuń"
        },
        "cancel": {
        "en": "Cancel",
        "fr": "Cancel",
        "pl": "Porzuć"
        },
        "remove_question": {
        "en": "Do you want to remove selected user?",
        "fr": "Do you want to remove selected user?",
        "pl": "Czy chcesz usunąć wybranego użytkownika?"
        },
        "remove_info": {
        "en": "Are you sure?",
        "fr": "Are you sure?",
        "pl": "Na pewno?"
        },
        "remove_title": {
        "en": "Removing user",
        "fr": "Removing user",
        "pl": "Usuwanie użytkownika"
        }
        }
    </script>
</app_users>
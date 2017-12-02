<app_users>
    <div class="row" if={ selected }>
         <div class="col-md-12">
            <user_form ref="user_edit"></user_form>
        </div>
    </div>
    <div class="row" if={ !selected }>
        <div class="col-md-12">
            <h2>{labels.title[app.language]} 
                <i class="fa fa-refresh" aria-hidden="true" onclick={ refreshUsers() }>&nbsp;</i>
                <i class="fa fa-plus" aria-hidden="true" onclick={ editUser('NEW', true) }>&nbsp;</i>
            </h2>
            <table id="doclist" class="table table-condensed">
                <thead>
                    <tr>
                        <th>{labels.t_uid[app.language]}</th>
                        <th>{labels.t_type[app.language]}</th>
                        <th>{labels.t_role[app.language]}</th>
                        <th>{labels.t_status[app.language]}</th>
                        <th class="text-right">
                            <i class="fa fa-plus" aria-hidden="true" onclick={ editUser('NEW', true) }></i>
                        </th>
                    </tr>
                </thead>
                <tbody>
                    <tr each={user in users}>
                        <td>{ user.uid }</td>
                        <td>{ user.type }</td>
                        <td>{ user.role }</td>
                        <td>{ user.authStatus }</td>
                        <td class="text-right">
                            <i class="fa fa-eye" aria-hidden="true" onclick={ editUser(user.uid, false) }>&nbsp;</i>
                            <i class="fa fa-pencil" aria-hidden="true" onclick={ editUser(user.uid, true) }>&nbsp;</i>
                            <i class="fa fa-trash" aria-hidden="true" onclick={ select(user.uid) } data-toggle="modal" data-target="#removeDialog">&nbsp;</i>
                        </td>
                    </tr>
                </tbody>
            </table>
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
        var self = this;
        self.listener = riot.observable();
        self.users = []
                self.selected = ''
                self.removing = ''
                self.selectedLanguage = 'EN'

                //globalEvents.on('pageselected:users', function (eventName) {
                this.on('mount', function(){
                self.selected = ''
                        console.log('PAGE USERS')
                        readUserList()
                });
        self.listener.on('*', function (eventName) {
        app.log('LISTENER: ' + eventName)
                switch (eventName){
        case 'submitted':
                self.selected = ''
                //readMyDevices()  //this line results in logout,login error
                break
                case 'cancelled':
                self.selected = ''
                break
                default:
                app.log('USERS: ' + eventName)
        }
        riot.update()
        });
        var readUserList = function () {
        //TODO
        app.log('reading docs ...')
                getData(app.userAPI, // url
                        null, // query
                        app.user.token, // token
                        updateList, // callback
                        self.listener, // event listener
                        'OK', // success event name
                        null, // error event name
                        app.debug, // debug switch
                        globalEvents         // application event listener
                        );
        }

        var updateList = function (text) {
        app.log("DOCUMENTS: " + text)
                self.users = JSON.parse(text)
                riot.update()
        }

        editUser(uid, allowEdit){
        //TODO
        return function(e){
        e.preventDefault()
                self.selected = uid
                riot.update()
                app.log('SELECTED FOR EDITING: ' + uid)
                self.refs.user_edit.init(self.listener, uid, allowEdit, true)
        }
        }

        select(uid){
            return function(e){
                e.preventDefault()
                self.removing = uid
                riot.update()
                console.log('DEL SELECTED ' + uid)
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
                console.log('REMOVING ' + self.removing + ' ...')
                if (self.removing == app.user.name){
        alert('Operation not permitted')
                self.removing = ''
        } else{
        deleteData(
                app.userAPI + '/' + self.removing,
                app.user.token,
                self.closeRemove,
                null, //self.listener, 
                'submit:OK',
                'submit:ERROR',
                app.debug,
                null //globalEvents
                )
        }
        }
        }

        self.closeRemove = function(object){
        var text = '' + object
                console.log('CALBACK: ' + object)
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
                "pl": "użytkownicy"
            },
            "t_uid": {
                "en": "LOGIN",
                "pl": "LOGIN"
            },
            "t_type": {
                "en": "TYPE",
                "pl": "TYP"
            },
            "t_role": {
                "en": "ROLE",
                "pl": "ROLA"
            },
            "t_status": {
                "en": "STATUS",
                "pl": "STATUS"
            },
            "remove": {
                "en": "Remove",
                "pl": "Usuń"
            },
            "cancel": {
                "en": "Cancel",
                        "pl": "Porzuć"
            },
            "remove_question": {
                "en": "Do you want to remove selected user?",
                        "pl": "Czy chcesz usunąć wybranego użytkownika?"
            },
            "remove_info": {
                "en": "Did you remove or transfer user's data and devices?",
                        "pl": "Czy usunąłeś lub przekazałeś dane oraz urządzenia użytkownika?"
            },
            "remove_title": {
                "en": "Removing user",
                        "pl": "Usuwanie użytkownika"
            }
        }
    </script>
</app_users>
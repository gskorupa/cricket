<app_login>
    <div class="panel panel-default form-login">
        <div class="panel-body">
            <form onsubmit={ submitLoginForm }>
                <h3 class="logo">{ labels.l_title[app.language] }</h3>
                <div class="form-group">
                    <label class="visuallyhidden" for="login">{ labels.l_name[app.language] }</label>
                    <input class="form-control" id="login" name="login" type="text" 
                               placeholder={ labels.l_name[app.language] } required>
                </div>
                <div class="form-group">
                    <label class="visuallyhidden" for="description">{ labels.l_password[app.language] }</label>
                    <input class="form-control" id="password" name="password" type="password" 
                               placeholder={ labels.l_password[app.language] } required>
                </div>
                <button type="submit" class="btn btn-block btn-primary">{ labels.l_save[app.language] }</button>
            </form>
        </div>
    </div>
    <script>
        self=this
        globalEvents.on('*', function (event) {
            if(event=='auth:loggedin'){
                app.currentPage = 'main'
                getData(app.userAPI+'/'+app.user.name, null, app.user.token, saveUserData, globalEvents)
            }else if(event=='auth:error'){
                alert(event)
            }
        });
        saveUserData = function(text){
            tmpUser = JSON.parse(text);
            app.user.role = tmpUser.role
            riot.update()
        }
        submitLoginForm = function(e){
            e.preventDefault()
            app.log("submitting ..."+e.target)
            loginSubmit(e.target, globalEvents, 'auth:loggedin', 'auth.error');
            e.target.reset()
        }
        this.labels = {
            "l_title": {
                "en": "Cricket Microsite",
                "fr": "Cricket Microsite",
                "pl": "Cricket Microsite"
            },
            "l_name": {
                "en": "Login",
                "fr": "Login",
                "pl": "Login"
            },
            "l_password": {
                "en": "Pasword",
                "fr": "Pasword",
                "pl": "Hasło"
            },
            "l_save": {
                "en": "Sign In",
                "fr": "Sign In",
                "pl": "Zaloguj się"
            },
            "l_error": {
                "en": "Wrong login or password",
                "en": "Wrong login or password",
                "pl": "Niepoprawny login lub hasło"
            }
        }
    </script>
    <style>
        .form-login{
            max-width: 300px;
            margin: 0 auto;
            text-align: center;
        }
        .logo{
            margin-bottom: 30px;
        }
        .visuallyhidden{
            border: 0;
            clip: rect(0,0,0,0);
            height: 1px;
            margin: -1px;
            overflow: hidden;
            padding: 0;
            position: absolute;
            width: 1px;
        }
    </style>
</app_login>

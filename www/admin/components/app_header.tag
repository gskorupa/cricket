<app_header>
        <nav class="navbar navbar-expand-md navbar-dark bg-primary fixed-top">
            <a class="navbar-brand" href="#">{ labels.name[app.language] }</a>
            <span hidden={ app.requests<=0 }><spinner/></span>
            <button class="navbar-toggler" type="button" data-toggle="collapse" data-target="#navbarNavDropdown" aria-controls="navbarNavDropdown" aria-expanded="false" aria-label="Toggle navigation">
                <span class="navbar-toggler-icon"></span>
            </button>
            <div class="collapse navbar-collapse" id="navbarNavDropdown">
                <ul class="navbar-nav">
                    <li class="nav-item"  data-toggle="collapse" data-target="#navbarNavDropdown" onclick="document.location = '#users';"`if={app.user.status == 'logged-in' && app.user.role.includes('admin')}>
                        <a class="nav-link" href="#users">{ labels.users[app.language] }</a>
                    </li>
                    <li class="nav-item dropdown" if={app.user.status == 'logged-in' && app.user.role.includes('redactor') }>
                        <a href="#" class="nav-link dropdown-toggle" id="navbarDropdownMenuLink" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">{ labels.content[app.language] }<span class="caret"></span></a>
                        <div class="dropdown-menu" aria-labelledby="navbarDropdownMenuLink">
                            <a href="#documents"class="dropdown-item" data-toggle="collapse" data-target="#navbarNavDropdown" onclick="document.location = '#documents';">{ labels.documents[app.language] }</a>
                            <a href="#tags"class="dropdown-item"  data-toggle="collapse" data-target="#navbarNavDropdown" onclick="document.location = '#tags';">{ labels.tags[app.language] }</a>
                        </div>
                    </li>
                    <li class="nav-item"  data-toggle="collapse" data-target="#navbarNavDropdown" onclick="document.location = '#logout';"if={app.user.status == 'logged-in'}>
                        <a class="nav-link" href="#logout">{ labels.logout[app.language] }</a>
                    </li>
                    <li class="nav-item"  data-toggle="collapse" data-target="#navbarNavDropdown" onclick="document.location = '#login';"if={app.user.status != 'logged-in'}>
                        <a class="nav-link" href="#login">{ labels.login[app.language] }</a>
                    </li>
                    <li class="nav-item" data-toggle="collapse" data-target="#navbarNavDropdown" onclick="document.location = '#en';" if={app.language!='en'}><a class="nav-link" href="#en">EN</a></li>
                    <li class="nav-item" data-toggle="collapse" data-target="#navbarNavDropdown" onclick="document.location = '#en';" if={app.language!='fr'}><a class="nav-link" href="#fr">FR</a></li>
                    <li class="nav-item" data-toggle="collapse" data-target="#navbarNavDropdown" onclick="document.location = '#pl';" if={app.language!='pl'}><a class="nav-link" href="#pl">PL</a></li>
                </ul>
            </div>
        </nav>
    <script>
        var self = this
        globalEvents.on('*', function (event) {
            if(event=='err:401'||event=='err:401'){
                app.user.name = '';
                app.user.token = '';
                app.user.status = 'logged-out';
                app.log('logged out')
                riot.update()
            }else if(event&&(event=='sending'||event=='dataLoaded'||event.startsWith('err:'))){
                app.log('HANDLING:'+event);
                riot.update()
            }else{
                app.log(event)
            }
        })
        this.labels = {
            "name": {
                "en": "Cricket \u00B5Site",
                "fr": "Cricket \u00B5Site",
                "pl": "Cricket \u00B5Site"
            },
            "users": {
                "en": "Users",
                "fr": "Users",
                "pl": "Użytkownicy"
            },
            "content": {
                "en": "Content",
                "fr": "Content",
                "pl": "Treści"
            },
            "documents": {
                "en": "documents",
                "fr": "documents",
                "pl": "dokumenty"
            },
            "tags": {
                "en": "tags",
                "fr": "tags",
                "pl": "znaczniki"
            },
            "login": {
                "en": "Sign In",
                "fr": "Sign In",
                "pl": "Zaloguj się"
            },
            "logout": {
                "en": "Sign Out",
                "fr": "Sign Out",
                "pl": "Wyloguj się"
            }
        }
    </script>
</app_header>

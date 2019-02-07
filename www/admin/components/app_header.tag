<app_header>
        <nav class="navbar navbar-expand-md navbar-dark bg-primary fixed-top">
            <a class="navbar-brand" href="#">{ app.config.brand }</a>
            <span hidden={ app.requests<=0 }><spinner/></span>
            <button class="navbar-toggler" type="button" data-toggle="collapse" data-target="#navbarNavDropdown" aria-controls="navbarNavDropdown" aria-expanded="false" aria-label="Toggle navigation">
                <span class="navbar-toggler-icon"></span>
            </button>
            <div class="collapse navbar-collapse" id="navbarNavDropdown">
                <ul class="navbar-nav">
                    <li class="nav-item"  data-toggle="collapse" data-target="#navbarNavDropdown" onclick="document.location = '#users';"`if={app.user.status == 'logged-in' && app.user.role.includes('admin')}>
                        <a class="nav-link" href="#users">{ app.texts.app_header.users[app.language] }</a>
                    </li>
                    <li class="nav-item"  data-toggle="collapse" data-target="#navbarNavDropdown" onclick="document.location = '#documents';"`if={app.user.status == 'logged-in' && app.user.role.includes('redactor')}>
                        <a class="nav-link" href="#documents">{ app.texts.app_header.documents[app.language] }</a>
                    </li>
                    <li class="nav-item"  data-toggle="collapse" data-target="#navbarNavDropdown" onclick="document.location = '#tags';"`if={app.user.status == 'logged-in' && app.user.role.includes('redactor')}>
                        <a class="nav-link" href="#tags">{ app.texts.app_header.tags[app.language] }</a>
                    </li>
                    <li class="nav-item"  data-toggle="collapse" data-target="#navbarNavDropdown" onclick="document.location = '#logout';"if={app.user.status == 'logged-in'}>
                        <a class="nav-link" href="#logout">{ app.texts.app_header.logout[app.language] }</a>
                    </li>
                    <li class="nav-item"  data-toggle="collapse" data-target="#navbarNavDropdown" onclick="document.location = '#login';"if={app.user.status != 'logged-in'}>
                        <a class="nav-link" href="#login">{ app.texts.app_header.login[app.language] }</a>
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
        
    </script>
</app_header>

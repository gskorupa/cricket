<app_header>
    <nav class="navbar navbar-default">
        <div class="container-fluid">
            <!-- Brand and toggle get grouped for better mobile display -->
            <div class="navbar-header">
                <button type="button" class="navbar-toggle collapsed" data-toggle="collapse" data-target="#navbar-links" aria-expanded="false">
                    <span class="icon-bar"></span>
                    <span class="icon-bar"></span>
                    <span class="icon-bar"></span>
                </button>
                <a class="navbar-brand" href="#">{ labels.name[app.language] }</a>        
            </div>
            <!-- Collect the nav links, forms, and other content for toggling -->
            <div class="collapse navbar-collapse" id="navbar-links">
                <ul class="nav navbar-nav navbar-right">
                    <li if={app.user.status == 'logged-in' && app.user.role.includes('admin')}>
                        <a href="#users" data-toggle="collapse" data-target="#navbar-links">{ labels.users[app.language] }</a></li>
                    <li if={app.user.status == 'logged-in' && app.user.role.includes('redactor') } class="dropdown">
                        <a href="#" class="dropdown-toggle" data-toggle="dropdown" role="button" aria-haspopup="true" aria-expanded="false">{ labels.content[app.language] }<span class="caret"></span></a>
                        <ul class="dropdown-menu">
                            <li><a href="#documents" data-toggle="collapse" data-target="#navbar-links">{ labels.documents[app.language] }</a></li>
                            <li><a href="#tags" data-toggle="collapse" data-target="#navbar-links">{ labels.tags[app.language] }</a></li>
                        </ul>
                    </li>
                    <li if={app.user.status == 'logged-in'}>
                        <a href="#logout" data-toggle="collapse" data-target="#navbar-links">{ labels.logout[app.language] }</a></li>
                    <li if={app.user.status != 'logged-in'}>
                        <a href="#login" data-toggle="collapse" data-target="#navbar-links">{ labels.login[app.language] }</a></li>
                    <li if={app.language!='en'}><a href="#en" data-toggle="collapse" data-target="#navbar-links">EN</a></li>
                    <li if={app.language!='pl'}><a href="#pl" data-toggle="collapse" data-target="#navbar-links">PL</a></li>
                </ul>
            </div><!-- /.navbar-collapse -->
        </div><!-- /.container-fluid -->
    </nav>
    <script>
        
        var self = this
        
        globalEvents.on('dataerror:401', function (eventName) {
            app.user.name = '';
            app.user.token = '';
            app.user.status = 'logged-out';
            riot.update();
        })
        
        this.labels = {
            "name": {
                "en": "Signomix : Management",
                "pl": "Signomix : Administracja"
            },
            "users": {
                "en": "Users",
                "pl": "Użytkownicy"
            },
            "content": {
                "en": "Content",
                "pl": "Treści"
            },
            "documents": {
                "en": "documents",
                "pl": "dokumenty"
            },
            "tags": {
                "en": "tags",
                "pl": "znaczniki"
            },
            "login": {
                "en": "Sign In",
                "pl": "Zaloguj się"
            },
            "logout": {
                "en": "Sign Out",
                "pl": "Wyloguj się"
            }
        }
    </script>
</app_header>

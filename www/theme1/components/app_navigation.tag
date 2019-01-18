<app_navigation>
    <nav class="navbar navbar-expand-md navbar-dark fixed-top bg-dark">
        <a class="navbar-brand" href="#">{ app.config.brand }</a>
        <button class="navbar-toggler" type="button" data-toggle="collapse" data-target="#navbarsExampleDefault" aria-controls="navbarsExampleDefault" aria-expanded="false" aria-label="Toggle navigation">
            <span class="navbar-toggler-icon"></span>
        </button>
        <div class="collapse navbar-collapse" id="navbarsExampleDefault">
            <ul class="navbar-nav mr-auto">
                <virtual each={item in app.navigation}>
                    <li class={ item.link===('#'+app.currentPage)?'nav-item active':'nav-item' } if={ item.link }>
                        <a class="nav-link" href={ item.link }>{ item.name }<span class="sr-only" if={ ('#'+app.currentPage)===item.link }>(current)</span></a>
                    </li>
                    <li class="nav-item dropdown" if={ item.options }>
                        <a class="nav-link dropdown-toggle" id={ item.id } data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">{ item.name }</a>
                        <div class="dropdown-menu" aria-labelledby={ item.id }>
                            <a each={ item.options } class="dropdown-item" href={ link }>{ name }</a>
                        </div>
                    </li>
                </virtual>
            </ul>
            <form class="form-inline my-2 my-lg-0">
                <input class="form-control mr-sm-2" type="text" placeholder="Search" aria-label="Search">
                <button class="btn btn-outline-success my-2 my-sm-0" type="submit">Search</button>
            </form>
        </div>
    </nav>
    <script>
        var self = this
        self.on('mount', function () {
            app.log('mounted')
            getData(app.url + '/config/site.json?language=' + app.language, null, null, setConfig, self)
            getData(app.url + '/config/navigation.json?language=' + app.language, null, null, setNavigation, self)
        })
        var setConfig = function (text) {
            app.config = JSON.parse(text)
            document.title=app.config.title
            riot.update()
        }
        var setNavigation = function (text) {
            app.navigation = JSON.parse(text)
            riot.update()
        }
    </script>
</app_navigation>

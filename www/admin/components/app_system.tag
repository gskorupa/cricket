<app_system>
    <div class="row">
        <div class="col-md-12">
            <h2>{app.texts.app_system_form.title[app.language]}</h2>
            <h5><i>{app.texts.app_system_form.message[app.language]}</i></h5>
            <ul class="nav nav-tabs">
                <li class="nav-item">
                    <a class="nav-link { active: activeTab=='config' }" onclick="{ selectConfig() }">Config</a>
                </li>
                <li class="nav-item">
                    <a class="nav-link { active: activeTab=='status' }" onclick="{ selectStatus() }">Status</a>
                </li>
                <li class="nav-item">
                    <a class="nav-link { active: activeTab=='database' }" onclick="{ selectDatabase() }">Databases</a>
                </li>
                <li class="nav-item">
                    <a class="nav-link { active: activeTab=='adapter' }" onclick="{ setAdapterProperty() }">Adapter</a>
                </li>
            </ul>
            <div class="row" if="{activeTab=='status'}">
                <div class="col-md-12">
                    <div class="card border-top-0">
                        <div class="card-body">
                            <form onsubmit={ switchMode }>
                                <div class="text-center">
                                <button type="submit" class="btn btn-primary">Switch to {(status.status=='online'?'maintenance':'online')} mode</button>
                                </div>
                            </form>
                            <pre style="width: 100%">{ JSON.stringify(status,null,'  ') }</pre>
                        </div>
                    </div>
                </div>
            </div>
            <div class="row" if="{activeTab=='config'}">
                <div class="col-md-12">
                    <div class="card border-top-0">
                        <div class="card-body">
                            <pre style="width: 100%">{ JSON.stringify(config,null,'  ') }</pre>
                        </div>
                    </div>
                </div>
            </div>
            <div class="row" if="{activeTab=='database'}">
                <div class="col-md-12">
                    <div class="card border-top-0">
                        <div class="card-body">
                            <form onsubmit={ submitForm }>
                                <div class="form-group">
                                    <label for="adapter">{ app.texts.app_system_form.db_adapter[app.language] }</label>
                                    <input class="form-control" id="adapter" name="adapter" type="text" value={ adapter } required>
                                </div>
                                <div class="form-group">
                                    <label for="query">{ app.texts.app_system_form.query[app.language] }</label>
                                    <textarea class="form-control cricket-article" rows="2" id="query" name="query">{ dbquery }</textarea>
                                </div>
                                <div class="text-center">
                                <button type="submit" class="btn btn-primary">{ app.texts.app_system_form.save[app.language] }</button>
                                <button type="button" class="btn btn-secondary" onclick={ close }>{ app.texts.app_system_form.cancel[app.language] }</button>
                                </div>
                            </form>
                            <pre style="width: 100%; margin-top: 1rem;">{ JSON.stringify(dbresponse,null,' ') }</pre>
                        </div>
                    </div>
                </div>
            </div>
            <div class="row" if="{activeTab=='adapter'}">
                <div class="col-md-12">
                    <div class="card border-top-0">
                        <div class="card-body">
                            <form onsubmit={ submitAdapterForm }>
                                <div class="form-group">
                                    <label for="adapter">{ app.texts.app_system_form.ad_adapter[app.language] }</label>
                                    <input class="form-control" id="adapter" name="name" type="text" required>
                                </div>
                                <div class="form-group">
                                    <label for="property">{ app.texts.app_system_form.ad_property[app.language] }</label>
                                    <input class="form-control" id="property" name="property">
                                </div>
                                <div class="form-group">
                                    <label for="value">{ app.texts.app_system_form.ad_value[app.language] }</label>
                                    <input class="form-control" id="value" name="value">
                                </div>
                                <div class="text-center">
                                <button type="submit" class="btn btn-primary">{ app.texts.app_system_form.save[app.language] }</button>
                                <button type="button" class="btn btn-secondary" onclick={ close }>{ app.texts.app_system_form.cancel[app.language] }</button>
                                </div>
                            </form>
                            <pre style="width: 100%; margin-top: 1rem;">{ JSON.stringify(adapterresponse,null,' ') }</pre>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
    <script charset="UTF-8">
        var self = this
        self.activeTab = 'config'
        self.status = {}
        self.config = {}
        self.dbquery = ''
        self.adapter = ''
        self.dbresponse = {}
        self.adapterresponse = {}
        self.listener = riot.observable()
        
        this.on('mount',function(){
            readConfig()
        })
        
        self.listener.on('*', function (eventName) {
            var info = ''+eventName
            if(info.startsWith('err:')){
                self.dbresponse=JSON.parse(info.substring(info.indexOf(' ')+1))
                riot.update()
            }
        })
        
        selectConfig(){
            return function(e){
                e.preventDefault()
                self.activeTab = 'config'
                readConfig()
            }
        }
        
        selectStatus(){
            return function(e){
                e.preventDefault()
                self.activeTab = 'status'
                readStatus()
            }
        }
        
        selectDatabase(){
            return function(e){
                e.preventDefault()
                self.activeTab = 'database'
                riot.update()
            }
        }
        
        setAdapterProperty(){
            return function(e){
                e.preventDefault()
                self.activeTab = 'adapter'
                riot.update()
            }
        }
        
        var readStatus = function () {
            var query = app.systemAPI + 'status'
            getData(query, null, app.user.token, updateStatus, self.listener)
        }

        var readConfig = function () {
            var query = app.systemAPI + 'config'
            getData(query, null, app.user.token, updateConfig, self.listener)
        }
        
        var updateStatus = function(text) {
            self.status = JSON.parse(text)
            setTimeout( function(){ riot.update(); }, 200);
        }
        
        var updateConfig = function(text) {
            self.config = JSON.parse(text)
            setTimeout( function(){ riot.update(); }, 200);
        }
        
        self.submitForm = function (e) {
            e.preventDefault()
            var fd = new FormData(e.target)
            self.dbquery=fd.query
            self.adapter=fd.adapter
            sendFormData(fd, 'POST', app.systemAPI+'database', app.user.token, self.getDbResponse, self.listener)
        }

        self.getDbResponse = function (object) {
            var text = '' + object
            if (text.startsWith('{')) {
                self.dbresponse = JSON.parse(text)
            }else if (text.startsWith('[')) {
                self.dbresponse = JSON.parse(text)
            } else if (text.startsWith('[object MouseEvent')) {
                self.listener.trigger('cancelled')
            } else if (text.startsWith('err:')) {
                self.dbresponse={"error":text}
            }else{
                //console.log(text)
            }
            setTimeout( function(){ riot.update(); }, 200);
        }
        
        self.submitAdapterForm = function (e) {
            e.preventDefault()
            var fd = new FormData(e.target)
            self.dbquery=fd.query
            self.adapter=fd.adapter
            sendFormData(fd, 'POST', app.systemAPI+'adapter', app.user.token, self.getAdapterResponse, self.listener)
        }
        
        self.getAdapterResponse = function (object) {
            var text = '' + object
            if (text.startsWith('{')) {
                self.adapterresponse = JSON.parse(text)
            }else if (text.startsWith('[')) {
                self.adapterresponse = JSON.parse(text)
            } else if (text.startsWith('[object MouseEvent')) {
                self.listener.trigger('cancelled')
            } else if (text.startsWith('err:')) {
                self.adapterresponse={"error":text}
            }else{
                //console.log(text)
            }
            setTimeout( function(){ riot.update(); }, 200);
        }

        self.switchMode = function (e) {
            e.preventDefault()
            var fd = new FormData(e.target)
        if(self.status.status=='online'){
            fd.set('status','maintenance')
        }else if(self.status.status=='maintenance'){
            fd.set('status','online')    
        }else{
            return
        }
        sendFormData(fd, 'POST', app.systemAPI+'status', app.user.token, self.getModeResponse, self.listener)
        }
    
        self.getModeResponse = function (object) {
            var text = '' + object
        if (text.startsWith('{')) {
            self.status = JSON.parse(text)
        } else if (text.startsWith('[object MouseEvent')) {
            self.listener.trigger('cancelled')
        } else if (text.startsWith('error')) {
            alert(text)
        }
        setTimeout( function(){ riot.update(); }, 200);
        }
    
    </script>
</app_system>
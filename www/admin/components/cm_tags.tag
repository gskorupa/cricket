<cm_tags>
    <div class="row">
        <div class="col-md-12">
            <h2>{app.texts.cm_tags.title[app.language]}</h2>
            <form class="form-inline">
                <label class="mr-2" for="ststusesDropdown">{ app.texts.cm_tags.l_status[app.language] }</label>
                <select class="select" id="statusesDropdown" onchange={ selectStatus }>
                    <option each={ tmpStatus, index in statuses }>{ tmpStatus }</option>
                </select>
                <virtual each={ lang, i in app.languages}>
                    &nbsp;<button type="button" class="btn btn-sm { lang==selectedLanguage?'btn-primary':'btn-secondary' }" onclick={ selectLanguage(lang) }>{ lang }</button>
                </virtual>
                <i class="material-icons clickable" onclick={ refreshTags() }>refresh</i>
            </form>
        </div>
    </div>
    <div class="row">
        <div class="col-md-12">
            <div class="card topspacing-sm">
                <div class="card-body">
                    <virtual each={doctag in doctags}><a class="clickable" onclick={ selectDoctag(doctag) }>{doctag}</a>&nbsp;</virtual>
                </div>
            </div>
        </div>
    </div>
    <div class="row">
        <div class="col-md-12">
            <table id="doclist" class="table table-condensed topspacing-sm">
                <thead>
                    <tr class="d-flex">
                        <th class="col-1">{app.texts.cm_tags.t_type[app.language]}</th>
                        <th class="col-3">{app.texts.cm_tags.t_name[app.language]}</th>
                        <th class="col-5">{app.texts.cm_tags.t_title[app.language]}</th>
                        <th class="col-3 text-right">{app.texts.cm_tags.t_uid[app.language]}</th>
                    </tr>
                </thead>
                <tbody>
                    <tr class="d-flex" each={doc in documents}>
                        <td class="col-1">
                            <i class="material-icons clickable" if={ doc.type == 'FILE'}>attachment</i>
                            <i class="material-icons clickable" if={ doc.type == 'CODE'}>receipt</i>
                            <i class="material-icons clickable" if={ doc.type == 'ARTICLE'}>subject</i>
                        </td>
                        <td class="col-3">{ doc.name }</td>
                        <td class="col-5">{ decodeURIComponent(doc.title) }</td>
                        <td class="col-3 text-right">{ doc.uid }</td>
                    </tr>
                </tbody>
            </table>
        </div>
    </div>
    <script charset="UTF-8">
        var self = this;
        self.listener = riot.observable();
        self.status = 'wip'
        self.statuses = ['wip', 'published']
        self.doctags = []
        self.documents = []
        self.selectedLanguage = 'EN'
        self.selectedDoctag =''

        //globalEvents.on('pageselected:documents', function (eventName) {
        this.on('mount', function(){
            self.selectedLanguage = app.language
            app.log(self.statuses)
            readTags()
        });
        
        this.on('unmount',function(){
            Object.keys(self.refs).forEach(function(key) {
                self.refs[key].unmount()
            });
            self.refs=[]
        })
        
        self.listener.on('*', function (eventName) {
            app.log('LISTENER: ' + eventName)
            if(!eventName) return;
            app.log('DOCUMENTS: ' + eventName)
            riot.update()
        });
        
        refreshTags(){
            return function(e){
                readTags()
            }
        }
        
        var readTags = function () {
            app.log('reading doctags ...')
            getData(app.cmAPI+'?tagsonly=true',null,app.user.token,updateTags,self.listener)
        }
        
        var updateTags = function (text) {
            self.doctags = JSON.parse(text)
            riot.update()
            console.log(self.doctags)
        }
        
        selectLanguage(newLanguage){
            return function(e){
                e.preventDefault()
                self.selectedLanguage = newLanguage
                readContentList()
            }
        }
        
        self.selectStatus = function(e){
            var index = e.target.selectedIndex
            self.status = e.target.options[index].value
            readContentList()
        }

        selectDoctag(newTag){
            return function(e){
                e.preventDefault()
                self.selectedDoctag = newTag
                readContentList()
            }
        }
        
        var readContentList = function () {
            app.log('reading docs ...')
            if( self.selectedDoctag == ''){
                return
            }
            var query = app.cmAPI+'?tag='+self.selectedDoctag+'&language='+self.selectedLanguage+'&status='+self.status
            getData(query,null,app.user.token,updateList,self.listener)
        }
        
        var updateList = function (text) {
            app.log("DOCUMENTS: " + text)
            self.documents = JSON.parse(text)
            riot.update()
        }
        
    </script>
</cm_tags>
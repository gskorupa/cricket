<app_main>
    <div class="row" >
        <div class="col-md-12">
            <!--<cs_article class="container" ref="homeart" path='/admin/home' language={ app.language }></cs_article>-->
            <p>Cricket version: cricket-version<br>Service version: app-version</p> <!-- will be modified by the build script -->
        </div>
    </div>
    <script charset="UTF-8">
        var self = this;
        self.listener = riot.observable();
        
        var self = this;
        
        this.on('unmount',function(){
            app.log('MAIN UNMOUNT')
            Object.keys(self.refs).forEach(function(key) {
                self.refs[key].unmount()
            });
            self.refs=[]
        })
        this.on('mount',function(){
            app.log('MAIN MOUNT')
            self.loadDocuments()
        })
        
        self.loadDocuments = function(){
            console.log(self.refs)
            Object.keys(self.refs).forEach(function(key) {
                self.refs[key].updateContent()
            });
        }

        self.labels = {
            "description": {
                "en": "Microsite management module",
                "pl": "Microsite -moduł administracyjny"
            }
        }
    </script>
</app_main>
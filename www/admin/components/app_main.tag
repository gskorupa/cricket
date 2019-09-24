<app_main>
    <div class="row" >
        <div class="col-md-12 text-center">
            <h1>Administration</h1>
        </div>
    </div>
    <div class="row" >
        <div class="col-md-12 text-center">
            <img src="resources/logo.svg" width="280px" alt="logo"/>
        </div>
    </div>
    <div class="row" >
        <div class="col-md-12 text-center">
            <p style="margin-top: 2em;">Cricket version: cricket-version <!-- will be modified by the build script -->
            </p>
            <p>Running on Java { app.jvm }</p>
            <a href="http://www.cricketmsf.org">http://www.cricketmsf.org</a>
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
            app.log(self.refs)
            Object.keys(self.refs).forEach(function(key) {
                self.refs[key].updateContent()
            });
        }
    </script>
</app_main>
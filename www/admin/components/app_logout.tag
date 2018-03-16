<app_logout>
    <div class="row" >
        <div class="col-md-12">logout</div>
    </div>
    <script charset="UTF-8">
        var self = this;
        
        this.on('mount',function(){
            app.log("LOGOUT");
            sendLogout();
        });
        
        sendLogout = function () {
            deleteData(app.authAPI+'/'+app.user.token,app.user.token,self.redirect,globalEvents)
        }

        self.redirect = function (event) {
            app.log("LOGOUT!");
            app.user.name=null;
            app.user.token=null;
            app.user.status='logged-out';
            route('main')
        }

    </script>
</app_logout>
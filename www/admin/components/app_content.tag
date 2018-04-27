<app_content>
    <main>
        <div  class="container white" if={ infoToShow }>
              <div class="row">
                <div class="col-md-12">
                    <div class="card red lighten-1 text-center z-depth-2">
                        <div class="card-body">
                            <p class="white-text mb-0">{labels.message[app.language]}</p>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        <div  class="container-fluid topspacing" if={ app.currentPage=='main'}><app_main></app_main></div>
        <div  class="container-fluid topspacing" if={ app.currentPage=='documents'}><cm_documents></cm_documents></div>
        <div  class="container-fluid topspacing" if={ app.currentPage=='tags'}><cm_tags></cm_tags></div>
        <div  class="container-fluid topspacing" if={ app.currentPage=='users'}><app_users></app_users></div>
        <div  class="container-fluid topspacing" if={ app.currentPage=='login'}><app_login></app_login></div>
        <div  class="container-fluid topspacing" if={ app.currentPage=='logout'}><app_logout></app_logout></div>
    </main>
    <script>
        var self = this
        self.infoToShow = false
        globalEvents.on('pageselected', function (event) {
            self.infoToShow = false
            riot.update()
        })
        globalEvents.on('err:403', function (eventName) {
            self.infoToShow = true
            riot.update()
        })
        self.labels = {
            "message": {
                "en": "Your session has expired. Please log in again.",
                "fr": "Votre session est expirée. Veuillez vous reconnecter.",
                "pl": "Twoja sesja wygasła. Zaloguj się ponownie"
            }
        }
    </script>
</app_content>
<app_content>
    <main>
        <div  class="container white" if={ infoToShow }>
              <div class="row">
                <div class="col-md-12">
                    <div class="card red lighten-1 text-center z-depth-2">
                        <div class="card-body">
                            <p class="white-text mb-0">{labels.message1[app.language]}<a href="/admin">{labels.message2[app.language]}</a></p>
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
            alert(self.labels.timeoutmessage[app.language])
            riot.update()
        })
        self.labels = {
            "message1": {
                "en": "Your session has expired. ",
                "fr": "Votre session est expirée. ",
                "pl": "Twoja sesja wygasła. "
            },
            "message2": {
                "en": "Please log in again.",
                "fr": "Veuillez vous reconnecter.",
                "pl": "Zaloguj się ponownie."
            },
            "timeoutmessage": {
            "en": "Your session has expired. Unsaved changes will be lost.",
            "fr": "Votre session est expirée. Les changements non indiqués seront perdus.",
            "pl": "Twoja sesja wygasła. Niezapamiętane zmiany zostaną utracone."
        }
        }
    </script>
</app_content>
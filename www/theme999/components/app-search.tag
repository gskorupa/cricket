<app-search>
    <div class="container top-spacing">
        <div class="row">
            <div class="col-md-12">
                Search query: { app.searchquery }
            </div>
        </div>
    </div>
    <script>
        globalEvents.on('searched', function (event) {
            riot.update()
        })
    </script>
</app-search>

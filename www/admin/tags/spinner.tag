<spinner>
    <div class="loader">...</div>
    <!-- https://github.com/lukehaas/css-loaders -->
    <style>
        .loader,
        .loader:after {
            border-radius: 50%;
            width: 4em;
            height: 4em;
        }
        .loader {
            margin: 1px auto;
            font-size: 5px;
            position: relative;
            text-indent: -9999em;
            border-top: 0.8em solid rgba(255, 255, 255, 0.2);
            border-right: 0.8em solid rgba(255, 255, 255, 0.2);
            border-bottom: 0.8em solid rgba(255, 255, 255, 0.2);
            border-left: 0.8em solid #ffffff;
            -webkit-transform: translateZ(0);
            -ms-transform: translateZ(0);
            transform: translateZ(0);
            -webkit-animation: load8 1.1s infinite linear;
            animation: load8 1.1s infinite linear;
        }
        @-webkit-keyframes load8 {
            0% {
                -webkit-transform: rotate(0deg);
                transform: rotate(0deg);
            }
            100% {
                -webkit-transform: rotate(360deg);
                transform: rotate(360deg);
            }
        }
        @keyframes load8 {
            0% {
                -webkit-transform: rotate(0deg);
                transform: rotate(0deg);
            }
            100% {
                -webkit-transform: rotate(360deg);
                transform: rotate(360deg);
            }
        }
    </style>
</spinner>

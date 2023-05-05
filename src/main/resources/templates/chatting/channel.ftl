<!doctype html>
<html lang="en">
  <head>
    <title>Websocket Chat</title>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no">
    <!-- CSS -->
    <link rel="stylesheet" href="/webjars/bootstrap/5.2.2/dist/css/bootstrap.min.css">
    <style>
      [v-cloak] {
          display: none;
      }
    </style>
  </head>
  <body>
    <div class="container" id="app" v-cloak>
        <div class="row">
            <div class="col-md-12">
                <h3>채팅방 리스트</h3>
            </div>
        </div>
        <div class="input-group">
            <div class="input-group-prepend">
                <label class="input-group-text">방제목</label>
            </div>
            <input type="text" class="form-control" v-model="channel_name" v-on:keyup.enter="createChannel">
            <div class="input-group-append">
                <button class="btn btn-primary" type="button" @click="createChannel">채팅방 개설</button>
            </div>
        </div>
        <ul class="list-group">
            <li class="list-group-item list-group-item-action" v-for="item in channels" v-bind:key="item.channelCd" v-on:click="enterChannel(item.channelCd)">
                {{item.name}}
            </li>
        </ul>
    </div>
    <!-- JavaScript -->
    <script src="/webjars/vue/2.6.14/dist/vue.min.js"></script>
    <script src="/webjars/axios/0.21.1/dist/axios.min.js"></script>
    <script>
        var vm = new Vue({
            el: '#app',
            data: {
                channel_name : '',
                channels: [
                ]
            },
            created() {
                this.findAllChannel();
            },
            methods: {
                findAllChannel: function() {
                    axios.get('/channels').then(response => { this.channels = response.data; });
                },
                createChannel: function() {
                    if("" === this.channel_name) {
                        alert("방 제목을 입력해 주십시요.");
                        return;
                    } else {
                        var params = new URLSearchParams();
                        params.append("name",this.channel_name);
                        axios.post('/channel', params)
                        .then(
                            response => {
                                alert(response.data.name+"방 개설에 성공하였습니다.")
                                this.channel_name = '';
                                this.findAllChannel();
                            }
                        )
                        .catch( response => { alert("채팅방 개설에 실패하였습니다."); } );
                    }
                },
                enterChannel: function(channelCd) {
                    var sender = prompt('대화명을 입력해 주세요.');
                    if(sender != "") {
                        localStorage.setItem('wschat.sender',sender);
                        localStorage.setItem('wschat.channelCd',channelCd);
                        location.href="/channel/enter/"+channelCd;
                    }
                }
            }
        });
    </script>
  </body>
</html>
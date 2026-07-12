$(document).ready(function () {
//    $("#postReview").on("click", function () {
//        var commentInput = $("#reviewInput").val();
//        $.ajax({
//            type: 'POST',
//            url: 'csrf/review',
//            data: JSON.stringify({text: commentInput}),
//            contentType: "application/json",
//            dataType: 'json'
//        }).then(
//            function () {
//                getChallenges();
//                $("#commentInput").val('');
//            }
//        )
//    });

    getChallenges();

    function getChallenges() {
        $("#list").empty();
        $.get('csrf/review', function (result, status) {
            for (var i = 0; i < result.length; i++) {
                var comment = $('<li>', {'class': 'comment'});
                var avatar = $('<div>', {'class': 'pull-left'}).append(
                    $('<img>', {
                        'class': 'avatar',
                        src: 'images/avatar1.png',
                        alt: 'avatar'
                    })
                );
                var heading = $('<div>', {'class': 'comment-heading'}).append(
                    $('<h4>', {'class': 'user'}).text(result[i].user + ' / ' + result[i].stars + ' stars'),
                    $('<h5>', {'class': 'time'}).text(result[i].dateTime)
                );
                var body = $('<div>', {'class': 'comment-body'}).append(
                    heading,
                    $('<p>').text(result[i].text)
                );
                comment.append(avatar, body);
                $("#list").append(comment);
            }

        });
    }
})

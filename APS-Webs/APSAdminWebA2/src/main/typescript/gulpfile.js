/**
 * Current status:
 *
 *   including the browserified script rather than each individual "component/lib" separately causes
 *   a different result with errors I don't get from loading individual scripts. So something gets
 *   destroyed in the conversion. Maybe browserify is not compatible with Typescript transpiled code.
 */
var gulp = require('gulp');
var rename = require("gulp-rename");
var browserify = require('gulp-browserify');

gulp.task('prod', function() {
    gulp.src('app/main.js').pipe(browserify()).pipe(uglify()).pipe(gulp.dest('static'))
});

gulp.task('dev', function() {
    // gulp.src('app/main.js').pipe(browserify()).pipe(gulp.dest('static'))

    gulp.src('app/main.js')
        .pipe(rename("bundle.js"))
        .pipe(browserify())
        .pipe(gulp.dest('static'))
});

gulp.task("watch", function() {
    gulp.watch('app/**/*.js', ['dev']);
});

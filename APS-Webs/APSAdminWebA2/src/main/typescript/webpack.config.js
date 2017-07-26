const path = require('path');

module.exports = {
    entry: "./app/main.js",
    output: {
        path: path.resolve(__dirname, 'dist'),
        filename: 'bundle.js'
    },
    watch: false,
    devServer: {
        contentBase: '.'
    }
};

module.loaders = [
    {
        test: /\.tst$/,
        exclude: /node_modules/,
        loader: 'ts-loader'
    },
    // {
    //     test: /\.js$/,
    //     include: /node_modules/,
    //     loaders: ['strip-sourcemap-loader']
    // }
];


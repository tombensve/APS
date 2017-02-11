const path = require('path');

module.exports = {
    entry: "./app/main",
    output: {
        path: '/LVolumes/Development/projects/OSGi/APS-Dev/APS-Webs/APSAdminWebA2/src/main/typescript/static',
        filename: 'adminweb-bundle.js'
    },
    watch: true,
    devServer: {
        contentBase: '.'
    }
};

module.loaders = [
    {
        test: /\.ts$/,
        exclude: /node_modules/,
        loader: 'ts-loader'
    }
];



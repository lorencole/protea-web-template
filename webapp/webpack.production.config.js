var webpack = require('webpack');
var path = require('path');
var loaders = require('./webpack.loaders');
var WebpackStripLoader = require('strip-loader');

var stripLoader = {
 test: [/\.js$/, /\.es6$/],
 exclude: /node_modules/,
 loader: WebpackStripLoader.loader('console.log')
}

module.exports = {
	entry: [
		'./index.jsx' // Your appʼs entry point
	],
	output: {
		path: path.join(__dirname, 'public'),
		filename: 'bundle.js'
	},
	resolve: {
		extensions: ['', '.js', '.jsx']
	},
	module: {
		loaders: [loaders, stripLoader]
	}
};

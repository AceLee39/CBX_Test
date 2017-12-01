/**
 * ANT JS to be used to re-index the sql files inside a folder
 * This script will find all the SQL file with pattern:
 *        (.*)-@filetype-([0-9]{0,2}-)([0-9]{0,2}-)?(.*)
 *   and rename to:
 *        $1-@filetype-$4
 *
 * Follow attributes are need:
 * folder - Location of the SQL folder
 * filetype - either to be 'update' or 'install' (default be be 'update')
 */
importClass(java.io.File);

var params = {};
//-- set default value for the input attributes (Note that all attributes should be in lower-case)
params.folder = '' + attributes.get('folder');
params.filetype = attributes.get('filetype') || 'update';
self.log('folder=' + params.folder);
self.log('filetype=' + params.filetype);

reIndexSql(params.folder, params.filetype);

function reIndexSql(folder, fileType) {
    var fs = project.createDataType("fileset");
    fs.setIncludes( "*.sql" );
    fs.setDir(new File(folder));
    var files = fs.getDirectoryScanner(project).getIncludedFiles( );
    for (var i = 0; i < files.length; i++) {
        var filename = files[i];
        var idx = i + 1;
        var padIdx = (idx < 10) ? "0" + idx: idx;
        var newFilename = filename.replaceAll("(.*)-" + fileType + "-([0-9]{0,2}-)([0-9]{0,2}-)?(.*)", "$1-update-" + padIdx + "-$4");
        //self.log('newFilename=' + newFilename);
        var file = new File(folder, filename);
        var newFile = new File(folder, newFilename);
        file.renameTo(newFile);
    }
    self.log("Finished re-indexing the SQL files");
}

/**
 * ANT JS to be used to scan the Excel folder and generate correspodning SQL files
 * Follow attributes are need:
 * excelfolder - Location of the Excel folder
 * startmodule - Module to be start generate. Useful for re-generate the script.
 *               Default to be empty string (generate all the mdoules found in Excel folder)
 *               e.g. startmodule = "vpo",
 *                    Program will generate SQL files from vpo module
 * gentype - Type in comma separated format to be generated.
 *           Available values are: entity, form, inheritance, view, security, validation, system, popup
 *           Default to be all.
 * dbuser - DB user to run against
 * dbpwd  - Password of the DB user
 * dburl  - URL of the DB
 * overwritesql - Whether to check and overwrite the generated SQL (default to be false)
 * modules - modules to be generated
 * popups - popup to be generated
 * diffcvstxtfile - location of the CVS Txt file (e.g. ${release.dir}/updated-excel.txt)
 *                  If ${diffcvstxtfile} is found, file will be generated according to the content
 *                  of the ${diffcvstxtfile} file
 *
 * Following is an example to define the scriplet in ANT
    <scriptdef name="scanAndGenExcelSql" language="javascript"
               src="${basedir}/ant-lib/scan-and-gen-excel-sql.js"
               description="Scan Excel folder and generate SQL">
        <!-- Note: All attributes will be in lower case.
                   When use, it can be mixed of upper and lower case.
                   e.g. outputpath ==> outputPath
        -->
        <attribute name="excelfolder"/>
        <attribute name="startmodule"/>
        <attribute name="outputpath"/>
        <attribute name="gentype"/>
        <attribute name="modules"/>
        <attribute name="dbuser"/>
        <attribute name="dbpwd"/>
        <attribute name="dburl"/>
        <attribute name="overwritesql"/>
        <attribute name="popups"/>
        <attribute name="diffcvstxtfile"/>
    </scriptdef>
 *
 * Note: This scriptlet also depends on the 'runGenForm' macro defined in the build.xml
 */
var javaVersion = java.lang.System.getProperty("java.version");
self.log(javaVersion);
if (!javaVersion.startsWith("1.7")) {
    load("nashorn:mozilla_compat.js");
}

importClass(java.io.File);
importClass(java.lang.Thread);
importClass(java.io.BufferedReader);
importClass(java.io.FileReader);
importClass(java.io.FileOutputStream);

var globalParams = getParams();
var moduleMap = {};
if (isGoingToGen('entity')) {
    self.log("Checking the existence of the Entity Excel files");
    constructModuleMap(globalParams.diffCVSTxtFile, "/entity", "_entity.xls", "entity", moduleMap);
}
if (isGoingToGen('form')) {
    self.log("Checking the existence of the Form Excel files");
    constructModuleMap(globalParams.diffCVSTxtFile, "/form", "_form.xls", "form", moduleMap);
}
if (isGoingToGen('inheritance')) {
    self.log("Checking the existence of the Inheritance Excel files");
    constructModuleMap(globalParams.diffCVSTxtFile, "/inheritance", "_dataMappingRule.xls", "inheritance", moduleMap);
}
if (isGoingToGen('view')) {
    self.log("Checking the existence of the View Excel files");
    constructModuleMap(globalParams.diffCVSTxtFile, "/searchView", "_view.xls", "view", moduleMap);
}
if (isGoingToGen('security')) {
    self.log("Checking the existence of the Security Excel files");
    constructModuleMap(globalParams.diffCVSTxtFile, "/security", "_entity_security.xls", "acl-entity", moduleMap);
    constructModuleMap(globalParams.diffCVSTxtFile, "/security", "_form_security.xls", "acl-form", moduleMap);
    constructModuleMap(globalParams.diffCVSTxtFile, "/security", "_view_security.xls", "acl-view", moduleMap);
}
if (isGoingToGen('validation')) {
    self.log("Checking the existence of the Validation Excel files");
    constructModuleMap(globalParams.diffCVSTxtFile, "/validation", "_validation.xls", "validation", moduleMap);
}

generateForm(moduleMap);

if (isGoingToGen('apiRepresentation')){
    var apiModuleMap = {};
    constructModuleMap(globalParams.diffCVSTxtFile,"/api","_api.xls","api",apiModuleMap);
    generateApi(apiModuleMap);
}

if (isGoingToGen('view')) {
    generateSystemSearchView(globalParams.diffCVSTxtFile);
}

if (isGoingToGen('popup')) {
    var poupModuleMap = {};
    constructModuleMap(globalParams.diffCVSTxtFile, "/popup", "_popup.xlsx", "popup", poupModuleMap);
    generatePopup(poupModuleMap);
}

if (isGoingToGen('system')) {
    generateSystem(globalParams.diffCVSTxtFile);
}

//-- private functions
function getParams() {
    var params = {};
    //-- set default value for the input attributes (Note that all attributes should be in lower-case)
    params.excelFolder = attributes.get('excelfolder');
    params.startModule = attributes.get('startmodule');
    params.outputPath = attributes.get('outputpath');
    if (params.outputPath == null) {
        params.outputPath = project.getProperty('output.dir');
    }
    params.genType = attributes.get('gentype');
    if (params.genType == null || !params.genType || params.genType == '') {
        params.genType = 'entity,form,inheritance,view,security,validation,popup,system';
    } else {
        params.genType = params.genType.trim();
    }
    params.dbuser = attributes.get('dbuser') || '' + project.getProperty('gen.db.user');
    params.dbpwd = attributes.get('dbpwd') || '' + project.getProperty('gen.db.pwd');
    //params.dburl = attributes.get('dburl') || '' + project.getProperty('gen.db.url');
    params.dbname = attributes.get('dbname');
    params.dbservice = attributes.get('dbservice');
    params.dbport = attributes.get('dbport');
    var modulesStr = attributes.get('modules');
    params.modules = {};
    params.hasDefinedModules = (modulesStr != null) && modulesStr.length() > 0;
    var modulesArray = [];
    if (params.hasDefinedModules) {
        modulesArray = modulesStr.split(',');
        for (var i = 0; i < modulesArray.length; i++) {
            params.modules[modulesArray[i].trim()] = true;
        }
    }
    var popupsStr = attributes.get('popups');
    params.popups = {};
    params.hasDefinedPopups = (popupsStr != null) && popupsStr.length() > 0;
    var popupsArray = [];
    if (params.hasDefinedPopups) {
        popupsArray = popupsStr.split(',');
        for (var i = 0; i < popupsArray.length; i++) {
            params.popups[popupsArray[i].trim()] = true;
        }
    }
    params.sleepTime = project.getProperty('gen.form.sleep.time') || 30;
    params.sleepFreq = project.getProperty('gen.form.sleep.freq') || 10;
    params.overwriteSQL = attributes.get('overwritesql') == 'true';
    params.diffCVSTxtFile = attributes.get('diffcvstxtfile');
    params.genTryCatchForDDLAlter = attributes.get('gentrycatchforddlalter');

    self.log("excelFolder=" + params.excelFolder);
    self.log("outputPath=" + params.outputPath);
    self.log("genType=" + params.genType);
    self.log("modules=" + modulesStr);
    self.log("popups=" + popupsStr);
    self.log("hasDefinedModules=" + params.hasDefinedModules);
    self.log("overwriteSQL=" + params.overwriteSQL);
    self.log("genTryCatchForDDLAlter=" + params.genTryCatchForDDLAlter);
    return params;
}

function isGoingToGen(checkGenType) {
    return globalParams.genType.indexOf(checkGenType) > -1;
}

function generateForm(moduleMap) {
    var modules = [];
    for (var module in moduleMap) {
        modules.push(module);
    }
    modules.sort();
    var skipProcess = (globalParams.startModule != null && globalParams.startModule != "");
    var numModuleProcessed = 0;
    for (var i = 0; i < modules.length; i++) {
        var module = modules[i];

        if (skipProcess) {
            skipProcess = (module != globalParams.startModule);
        }

        if (skipProcess) {
            self.log("Skipping module: " + module);
            continue;
        }

        skipProcess = false;

        if (globalParams.hasDefinedModules && !globalParams.modules[module]) {
            //self.log("Not matched defined modules. Skipping module: " + module);
            continue;
        }

        if (module == 'test') {
            self.log('Skipping module: ' + module);
            continue;
        }

        self.log("Generate module: " + module + " (" + (i + 1) + "/" + modules.length + ")");
        var target = getTargetString(module, moduleMap[module]);
        self.log("Target: " + target);
        if (target.length == 0) {
            self.log("No target. Skipping module: " + module);
            continue;
        }

        genForm(module, target);
        numModuleProcessed++;
        if (globalParams.sleepFreq > 0 && (numModuleProcessed % globalParams.sleepFreq == 0)) {
            self.log("Sleep for " + (globalParams.sleepTime) + " seconds");
            Thread.sleep(globalParams.sleepTime * 1000);
            self.log("Resume to generate");
        }
    }
}

function generateApi(apiMap) {
    self.log("Going to generate api");
    var apis = [];
    for (var api in apiMap) {
        apis.push(api);
    }
    apis.sort();
    var numModuleProcessed = 0;
    for (var i = 0; i < apis.length; i++) {
        var api = apis[i];

        self.log("Generate api: " + api + " (" + (i + 1) + "/" + apis.length + ")");
        self.log("skip gen file path:"+globalParams.outputPath);
        if (skipGenFiles([
                globalParams.outputPath + "/scripts/api/DML_" + api + "_api.sql"
            ], [
                globalParams.excelFolder + "/api/" + api + "_api.xlsx"
            ])) {
            continue;
        }
        genForm(api, "apiRepresentation");
        touchFile(globalParams.outputPath + "/scripts/api/DML_" + api + "_api.sql");
        numModuleProcessed++;
        if (globalParams.sleepFreq > 0 && (numModuleProcessed % globalParams.sleepFreq == 0)) {
            self.log("Sleep for " + (globalParams.sleepTime) + " seconds");
            Thread.sleep(globalParams.sleepTime * 1000);
            self.log("Resume to generate");
        }
    }

    if (skipGenFiles([
            globalParams.outputPath + "/scripts/api/api.sql"
        ], [
            globalParams.excelFolder + "/api/api.xlsx"
        ])) {
        return;
    }
    genForm("", "apiRepresentation");
    touchFile(globalParams.outputPath + "/scripts/api/api.sql");
}

function generatePopup(popupMap) {
    self.log("Going to generate popup");
    var popups = [];
    for (var popup in popupMap) {
        popups.push(popup);
    }
    popups.sort();
    var numModuleProcessed = 0;
    for (var i = 0; i < popups.length; i++) {
        var popup = popups[i];

        if (globalParams.hasDefinedPopups && !globalParams.popups[popup]) {
            //self.log("Not matched defined modules. Skipping module: " + module);
            continue;
        }

        self.log("Generate popup: " + popup + " (" + (i + 1) + "/" + popups.length + ")");
        if (skipGenFiles([
                globalParams.outputPath + "/scripts/popup/" + popup + ".sql"
            ], [
                globalParams.excelFolder + "/popup/" + popup + "_popup.xlsx"
            ])) {
            continue;
        }
        genForm(popup, "popup");
        touchFile(globalParams.outputPath + "/scripts/popup/" + popup + ".sql");
        numModuleProcessed++;
        if (globalParams.sleepFreq > 0 && (numModuleProcessed % globalParams.sleepFreq == 0)) {
            self.log("Sleep for " + (globalParams.sleepTime) + " seconds");
            Thread.sleep(globalParams.sleepTime * 1000);
            self.log("Resume to generate");
        }
    }

    if (skipGenFiles([
            globalParams.outputPath + "/scripts/popup/popup.sql"
        ], [
            globalParams.excelFolder + "/popup/popup.xlsx"
        ])) {
        return;
    }
    genForm("", "popup");
    touchFile(globalParams.outputPath + "/scripts/popup/popup.sql");
}

function generateSystem(cvsTextFile) {
    self.log('generateSystem(' + cvsTextFile + ')');
    if (cvsTextFile != null && cvsTextFile.length() > 0) {
        self.log('cvsTextFile != null && cvsTextFile.length() > 0');
        if (!isTxtExist(cvsTextFile, "system.xlsx")
            && !isTxtExist(cvsTextFile, "security/system_security.xlsx")
        ) {
            self.log("Not going to gen the system and security");
            return;
        }
    }
    genForm("", "system,security");
}

function generateSystemSearchView(cvsTextFile) {
    self.log('generateSystemSearchView(' + cvsTextFile + ')');
    if (cvsTextFile != null && cvsTextFile.length() > 0) {
        self.log('cvsTextFile != null && cvsTextFile.length() > 0');
        if (!isTxtExist(cvsTextFile, "searchView/searchView.xlsx")) {
            self.log("Not going to gen the searchView.xlsx");
            return;
        }
    }
    genForm("", "searchView");
}

function isTxtExist(cvsTextFile, searchTxt) {
    var reader = null;
    try {
        reader = new BufferedReader(new FileReader(cvsTextFile));
        for (var line; (line = reader.readLine()) != null; ) {
            if (line.indexOf(searchTxt) > -1) {
                self.log("line=" + line);
                return true;
            }
        }
    } catch (e) {
        self.log('error in reading: ' + cvsTextFile);
        self.log(e);
        // throw e;
    } finally {
        if (reader != null) {
            reader.close();
        }
    }
    return false;
}

function genForm(module, target) {
    // Note: all attributes set will be in lower case. ANT will convert them to proper case
    var newTask = project.createTask("runGenForm");
    newTask.setDynamicAttribute("dbuser", globalParams.dbuser);
    newTask.setDynamicAttribute("dbpwd", globalParams.dbpwd);
    newTask.setDynamicAttribute("dbname", globalParams.dbname);
    newTask.setDynamicAttribute("dbservice", globalParams.dbservice);
    newTask.setDynamicAttribute("dbport", globalParams.dbport);
    newTask.setDynamicAttribute("outputpath", globalParams.outputPath);
    newTask.setDynamicAttribute("targetpath", globalParams.outputPath);
    newTask.setDynamicAttribute("module", module);
    newTask.setDynamicAttribute("target", target);
    newTask.setDynamicAttribute("exceldir", globalParams.excelFolder);
    newTask.setDynamicAttribute("gentrycatchforddlalter", globalParams.genTryCatchForDDLAlter);
    newTask.execute();
}

function constructModuleMap(cvsTextFile, folder, fileIndexSubstring, moduleType, moduleMap) {
    if (cvsTextFile != null && cvsTextFile.length() > 0) {
        try {
            retrieveModuleMapFromCVSDiffFile(cvsTextFile, folder, fileIndexSubstring, moduleType, moduleMap);
            return;
        } catch (e) {
            self.log("Cannot retrieveModuleMapFromCVSDiffFile");
            self.log(e);
        }
    }
    retrieveModuleMapIn(folder, fileIndexSubstring, moduleType, moduleMap);
}

function retrieveModuleMapFromCVSDiffFile(cvsTextFile, folder, fileIndexSubstring, moduleType, moduleMap) {
    var reader = null;
    try {
        var prefix = 'excel' + folder + "/";
        var prefix2 = folder.substring(1) + "/";
        reader = new BufferedReader(new FileReader(cvsTextFile));
        for (var line; (line = reader.readLine()) != null; ) {
            //self.log('line=' + line);
            var index0 = line.indexOf(prefix);
            if (index0 > -1) {
                var index = line.indexOf(fileIndexSubstring);
                if (index == -1) {
                    continue;
                }
                //self.log('prefix=' + prefix);
                index0 = index0 + prefix.length;

                var module = line.substring(index0, index);
                //self.log('>>> module = ' + module);
                addModuleMap(module, moduleType, moduleMap);
            }
            var index2 = line.indexOf(prefix2);
            if (index2 > -1) {
                var index = line.indexOf(fileIndexSubstring);
                if (index == -1) {
                    continue;
                }
                //self.log('prefix2=' + prefix2);
                index2 = index2 + prefix2.length;

                var module = line.substring(index2, index);
                //self.log('>>> module = ' + module);
                addModuleMap(module, moduleType, moduleMap);
            }
        }
    } catch (e) {
        self.log('error in reading: ' + cvsTextFile);
        throw e;
    } finally {
        if (reader != null) {
            reader.close();
        }
    }
}

function retrieveModuleMapIn(folder, fileIndexSubstring, moduleType, moduleMap) {
    var fs = project.createDataType("fileset");
    fs.setIncludes( "*.xls*" );
    fs.setDir(new File(globalParams.excelFolder, folder));
    var files = fs.getDirectoryScanner(project).getIncludedFiles( );
    for (var i = 0; i < files.length; i++) {
        var filename = files[i];
        var index = filename.indexOf(fileIndexSubstring);
        if (index == -1) {
            continue;
        }
        var module = filename.substring(0, index);
        addModuleMap(module, moduleType, moduleMap);
    }
}

function addModuleMap(module, value, moduleMap) {
    if (!moduleMap[module]) {
        moduleMap[module] = [];
    }
    moduleMap[module].push(value);
}

function getTargetString(module, moduleTypes) {
    var moduleTypeTargetStrMap = {
        "entity": "entity",
        "form": "form",
        "view": "searchView",
        "inheritance": "dataInheritanceProfile,dataMappingRule",
        "validation": "validation",
        "acl-entity": "security", "acl-form": "security", "acl-view": "security",
        //-- for backward compatible:
        "dataInheritanceProfile": "dataInheritanceProfile",
        "dataMappingRule": "dataMappingRule",
        "searchView": "searchView",
        "security": "security",
        "popup": "popup"
    };

    var targets = [];
    for (var i = 0; i < moduleTypes.length; i++) {
        var moduleType = moduleTypes[i];
        var mappedTargetStr = moduleTypeTargetStrMap[moduleType];
        var found = false;
        for (var j = 0; !found && j < targets.length; j++) {
            if (targets[j] == mappedTargetStr) {
                found = true;
            }
        }
        if (!found) {
            if (!globalParams.overwriteSQL && isCorrespondingGeneratedFilesFound(module, moduleType)) {
                continue;
            }
            targets.push(mappedTargetStr);
        }
    }
    return targets;
}

function isCorrespondingGeneratedFilesFound(module, moduleType) {
    if (moduleType == "entity") {
        return skipGenFiles([
            globalParams.outputPath + "/scripts/entity/DDL_ENTITY_" + module + ".sql",
            globalParams.outputPath + "/scripts/entity/DML_ENTITY_" + module + ".sql"
        ], [
            globalParams.excelFolder + "/entity/" + module + "_entity.xlsx"
        ]);
    }
    if (moduleType == "form") {
        return skipGenFiles([
            globalParams.outputPath + "/scripts/form/DML_FORM_" + module + ".sql"
        ], [
            globalParams.excelFolder + "/form/" + module + "_form.xlsx"
        ]);
    }
    if (moduleType == "searchView" || moduleType == "view") {
        return skipGenFiles([
            globalParams.outputPath + "/scripts/view/DML_ADVANCED_SEARCH_" + module + ".sql",
            globalParams.outputPath + "/scripts/view/DML_VIEW_" + module + ".sql"
        ], [
            globalParams.excelFolder + "/searchView/" + module + "_view.xlsx"
        ]);
    }
    if (moduleType == "inheritance") {
        return skipGenFiles([
            globalParams.outputPath + "/scripts/inheritance/DML_DATA_INH_" + module + ".sql",
            globalParams.outputPath + "/scripts/inheritance/DML_DATA_MAP_" + module + ".sql"
        ], [
            globalParams.excelFolder + "/inheritance/" + module + "_dataInheritanceProfile.xlsx",
            globalParams.excelFolder + "/inheritance/" + module + "_dataMappingRule.xlsx"
        ]);
    }
    if (moduleType == "dataInheritanceProfile") {
        return skipGenFiles([
            globalParams.outputPath + "/scripts/inheritance/DML_DATA_INH_" + module + ".sql"
        ], [
            globalParams.excelFolder + "/inheritance/" + module + "_dataInheritanceProfile.xlsx"
        ]);
    }
    if (moduleType == "dataMappingRule") {
        return skipGenFiles([
            globalParams.outputPath + "/scripts/inheritance/DML_DATA_MAP_" + module + ".sql"
        ], [
            globalParams.excelFolder + "/inheritance/" + module + "_dataMappingRule.xlsx"
        ]);
    }
    if (moduleType == "validation") {
        return skipGenFiles([
            globalParams.outputPath + "/scripts/validation/DML_VALIDATION_" + module + ".sql"
        ], [
            globalParams.excelFolder + "/validation/" + module + "_validation.xlsx"
        ]);
    }
    if (moduleType == "acl-entity") {
        return skipGenFiles([
            globalParams.outputPath + "/scripts/security/DML_ACL_" + module + "_entity.sql"
        ], [
            globalParams.excelFolder + "/security/" + module + "_entity_security.xlsx"
        ]);
    }
    if (moduleType == "acl-form") {
        return skipGenFiles([
            globalParams.outputPath + "/scripts/security/DML_ACL_" + module + "_form.sql"
        ], [
            globalParams.excelFolder + "/security/" + module + "_form_security.xlsx"
        ]);
    }
    if (moduleType == "acl-view") {
        return skipGenFiles([
            globalParams.outputPath + "/scripts/security/DML_ACL_" + module + "_view.sql"
        ], [
            globalParams.excelFolder + "/security/" + module + "_view_security.xlsx"
        ]);
    }
    if (moduleType == "security") {
        return skipGenFiles([
            globalParams.outputPath + "/scripts/security/DML_ACL_" + module + "_entity.sql",
            globalParams.outputPath + "/scripts/security/DML_ACL_" + module + "_form.sql",
            globalParams.outputPath + "/scripts/security/DML_ACL_" + module + "_view.sql"
        ], [
            globalParams.excelFolder + "/security/" + module + "_entity_security.xlsx",
            globalParams.excelFolder + "/security/" + module + "_form_security.xlsx",
            globalParams.excelFolder + "/security/" + module + "_view_security.xlsx"
        ]);
    }
    return false;
}

function skipGenFiles(files, excelFiles) {
    var fileMinLastModified = 0;
    for (var i = 0; i < files.length; i++) {
        var fileStr = files[i];
        var f = new File(fileStr);
        if (!f.exists()) {
            self.log("Cannot skip generate SQL file. File not found: " + fileStr);
            return false;
        }
        if (fileMinLastModified == 0 || fileMinLastModified > f.lastModified()) {
            fileMinLastModified = f.lastModified();
        }
    }
    var excelFileMaxLastModified = 0;
    for (var i = 0; i < excelFiles.length; i++) {
        var fileStr = excelFiles[i];
        var f = new File(fileStr);
        if (!f.exists()) {
            self.log("Skip generate SQL file. Excel file not found: " + fileStr);
            return true;
        }
        if (excelFileMaxLastModified < f.lastModified()) {
            excelFileMaxLastModified = f.lastModified();
        }
    }
    if (excelFileMaxLastModified >= fileMinLastModified) {
        self.log("Cannot skip generate SQL file. Excel file lastModified >= file lastModified.");
        return false
    }
    return true;
}

function touchFile(filename) {
    var file = new File(filename);
    try {
        file.getParentFile().mkdirs();
        if (!file.exists()) {
            new FileOutputStream(file).close();
        }
        file.setLastModified(new Date().getTime());
    } catch (e) {
        self.log("Cannot touch file: " + filename);
        self.log(e);
    }
}

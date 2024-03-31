class BasicInfoDto {
    constructor(props = {}) {
        this.version = props.version;
        this.acceptMaxFileSize = props.acceptMaxFileSize;
    }

    default() {
        return new BasicInfoDto({
            version: "",
            acceptMaxFileSize: 1024 * 1024 * 50,
        });

    }
}

class BasicSetting {
    constructor(props = {}) {
        this.multiFileMerge = props.multiFileMerge;
        this.expireTimeMinutes = props.expireTimeMinutes;
        this.deleteAfterUpload = props.deleteAfterUpload;
    }

    default() {
        return new BasicSetting({
            multiFileMerge: false,
            expireTimeMinutes: 60,
            deleteAfterUpload: true
        });
    }
}

class DragMaskStatus {
    constructor(props = {}) {
        this.lastDragEnter = props.lastDragEnter;
        this.showUploadMask = props.showUploadMask;
    }

    default() {
        return new DragMaskStatus({
            lastDragEnter: null,
            showUploadMask: false
        });
    }
}

class UploadingStatus {
    constructor(props = {}) {
        this.fileUploading = props.fileUploading;
        this.fileUploadPercent = props.fileUploadPercent;
    }

    default() {
        return new UploadingStatus({
            uploading: false,
            fileUploadPercent: 0,
        });
    }
}

class FileStatus {
    constructor(props = {}) {
        this.inputFullId = props.inputFullId;
        this.selectedFileName = props.selectedFileName;
    }

    default() {
        return new DataSimpleInfoDto({
            inputFullId: "",
            selectedFileName: ""
        });
    }
}

class ExportStatus {
    constructor(props = {}) {
        this.exportStart = props.exportStart;
        this.exportEnd = props.exportEnd;
        this.chooseAll = props.chooseAll;
        this.exporting = props.exporting;
        this.exportKey = props.exportKey;
        this.enableGroupByIndexes = props.enableGroupByIndexes;
        this.enableExportZip = props.enableExportZip;
        this.mergeDataList = props.mergeDataList;
    }

    default() {
        return new ExportStatus({
            exportStart: 1,
            exportEnd: 100,
            chooseAll: true,
            exporting: false,
            exportKey: ""
        });
    }
}

class PageInfo {
    constructor(props = {}) {
        this.rowCount = props.rowCount;
        this.totalCount = props.totalCount;
        this.page = props.page;
        this.totalPage = props.totalPage;
        this.dataLoading = props.dataLoading;
    }

    default() {
        return new PageInfo({
            rowCount: 100,
            page: 1,
            dataLoading: false,
        });
    }
}

class ConfirmData {
    constructor(props = {}) {
        this.title = props.title;
        this.content = props.content;
        this.action = props.action;
        this.data = props.data;
    }

    default() {
        return new ConfirmData({
            title: "",
            content: "",
            action: "",
            data: {},
        });
    }
}

class DataSimpleInfoDto {
    constructor(props = {}) {
        this.id = props.id;
        this.filename = props.filename;
        this.type = props.type;
        this.recordNums = props.recordNums;
        this.fileDeleted = props.fileDeleted;
        this.createTime = props.createTime;
        this.expireTime = props.expireTime;
        this.heads = props.heads ? props.heads.map(item => new DataCellDto(item)) : [];
    }
}

class UploadFileInfoDto {
    constructor(props = {}) {
        this.filename = props.filename;
        this.id = props.id;
        this.dataId = props.dataId;
        this.totalDataId = props.totalDataId;
        this.dataChecked = true;
        this.zipChecked = true;
    }

    getFullId() {
        if (!this.totalDataId === null || !this.totalDataId) {
            return "";
        }
        return this.totalDataId + "@" + this.dataId;
    }
}

class TotalUploadFileInfoDto {
    constructor(props = {}) {
        this.filename = props.filename;
        this.totalDataId = props.totalDataId;
        this.uploadFileInfoDtoList = props.uploadFileInfoDtoList ? props.uploadFileInfoDtoList.map(item => new UploadFileInfoDto(item)) : [];
        this.newlyAddedCount = props.newlyAddedCount;
    }
}

class DataDto {
    constructor(props = {}) {
        this.filename = props.filename;
        this.id = props.id;
        this.totalDataId = props.totalDataId;
        this.type = props.type;
        this.fileDeleted = props.fileDeleted;
        this.recordNums = props.recordNums;
        this.createTime = props.createTime;
        this.expireTime = props.expireTime;
        this.hasFixedHead = props.hasFixedHead;

        this.pageInfo = new PageInfo(props.pageInfo);

        this.heads = props.heads ? props.heads.map((item) => new DataCellDto(item, true)) : [];
        this.fixedHeads = props.fixedHeads ? props.fixedHeads.map((item) => new DataCellDto(item, true)) : [];
        this.colCounts = props.colCounts ? props.colCounts.map(item => item) : [];
        this.dataList = props.dataList ? props.dataList.map((item) => item ? item.map((item2) => new DataCellDto(item2)) : []) : [];
    }

    getUploadFileInfoDto() {
        return new UploadFileInfoDto({
            filename: this.filename,
            dataId: this.id,
            totalDataId: this.totalDataId
        });
    }
}

class TotalDataDto {
    constructor(props = {}) {
        this.filename = props.filename;
        this.id = props.id;

        this.dataDto = props.dataDto ? props.dataDto.map((item) => new DataDto(item)) : [];
    }
}

class DataCellDto {
    constructor(props = {}, isHead) {
        this.value = props.value;
        this.dataType = props.dataType;
        this.length = props.length;
        this.fixed = props.fixed;
        this.groupByChecked = false;
        this.shown = true;

        if (this.dataType === 6 && !isHead) {
            // 日期型数据
            this.value = parseTimeToStr(new Date(this.value).getTime());
        }
    }
}

class BatchDownloadInfoDto {
    constructor(props = {}) {
        this.id = props.id;
        this.dataId = props.dataId;
        this.filename = props.filename;
        this.groupByIndexes = props.groupByIndexes ? props.groupByIndexes.map(item => item) : [];
        this.dataDtoCount = props.dataDtoCount;
        this.totalDataCount = props.totalDataCount;
    }
}

class DbfFields {
    constructor(props = {}) {
        this.dataType = props.dataType;
        this.type = props.type;
        this.name = props.name;
    }
}

class DbfRowsDto {
    constructor(props = {}) {
        this.id = props.id;
        this.name = props.name;
        this.dbfFields = props.dbfFields ? props.dbfFields.map(item => new DbfFields(item)) : [];
        this.rows = props.rows;
    }
}

class ButtonExport {
    constructor(name, url) {
        this.name = name;
        this.url = url;
    }

    buildList(list) {
        if (!list) {
            return [];
        }

        let result = [];
        for (let i = 0; i < list.length; i++) {
            let item = list[i];
            result.push(new ButtonExport(item[0], item[1]));
        }
        return result;
    }
}
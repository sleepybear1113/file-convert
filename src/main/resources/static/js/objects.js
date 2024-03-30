class BasicInfoDto {
    constructor(props = {}) {
        this.version = props.version;
        this.acceptMaxFileSize = props.acceptMaxFileSize;
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

class PageInfo {
    constructor(props = {}) {
        this.rowCount = props.rowCount;
        this.totalCount = props.totalCount;
        this.page = props.page;
        this.totalPage = props.totalPage;
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
}
class DbfRecordInfoDto {
    constructor(props = {}) {
        this.fileDeleted = props.fileDeleted;
        this.hexId = props.hexId;
        this.name = props.name;
        this.recordNums = props.recordNums;
        this.createTime = props.createTime;
        this.expireTime = props.expireTime;
        this.dbfFields = props.dbfFields ? props.dbfFields.map(item => new DbfFields(item)) : [];
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

class DataDto {
    constructor(props = {}) {
        this.filename = props.filename;
        this.id = props.id;
        this.type = props.type;
        this.fileDeleted = props.fileDeleted;
        this.recordNums = props.recordNums;
        this.createTime = props.createTime;
        this.expireTime = props.expireTime;
        this.hasFixedHead = props.hasFixedHead;

        this.pageInfo = new PageInfo(props.pageInfo);

        this.heads = props.heads ? props.heads.map((item) => new DataCellDto(item)) : [];
        this.fixedHeads = props.fixedHeads ? props.fixedHeads.map((item) => new DataCellDto(item)) : [];
        this.colCounts = props.colCounts ? props.colCounts.map(item => item) : [];
        this.dataList = props.dataList ? props.dataList.map((item) => item ? item.map((item2) => new DataCellDto(item2)) : []) : [];

        this.selectedIndexes = new Array(this.heads.length).fill(false);
        this.groupByIndexes = new Array(this.heads.length).fill(false);
    }
}

class DataCellDto {
    constructor(props = {}) {
        this.value = props.value;
        this.dataType = props.dataType;
        this.length = props.length;
        this.fixed = props.fixed;
        this.checked = true;
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
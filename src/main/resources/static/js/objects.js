class DbfRecordInfoDto {
    constructor(props) {
        if (props == null) {
            return;
        }

        this.fileDeleted = props.fileDeleted;
        this.hexId = props.hexId;
        this.name = props.name;
        this.recordNums = props.recordNums;
        this.fileDeleted = props.fileDeleted;
        this.createTime = props.createTime;
        this.expireTime = props.expireTime;

        this.dbfFields = [new DbfFields()];
        this.dbfFields = buildDbfFields(props.dbfFields);
    }
}

/**
 * DataSimpleInfoDto 的 JavaScript 类
 */
class DataSimpleInfoDto {
    constructor(props) {
        if (props == null) {
            return;
        }

        this.id = props.id;
        this.filename = props.filename;
        this.type = props.type;
        this.recordNums = props.recordNums;
        this.fileDeleted = props.fileDeleted;
        this.createTime = props.createTime;
        this.expireTime = props.expireTime;

        this.heads = props.heads.map((item) => new DataCellDto(item));
    }
}

/**
 * DataDto 的 JavaScript 类
 */
class DataDto {
    constructor(props) {
        if (props == null) {
            return;
        }

        this.filename = props.filename;
        this.id = props.id;
        this.type = props.type;
        this.fileDeleted = props.fileDeleted;
        this.recordNums = props.recordNums;
        this.createTime = props.createTime;
        this.expireTime = props.expireTime;

        this.pageInfo = new PageInfo(props.pageInfo);

        this.heads = props.heads.map((item) => new DataCellDto(item));
        this.dataList = props.dataList.map((item) => item.map((item2) => new DataCellDto(item2)));
    }
}

/**
 * DataCellDto 的 JavaScript 类
 */
class DataCellDto {
    constructor(props) {
        if (props == null) {
            return;
        }

        this.value = props.value;
        this.dataType = props.dataType;
        this.length = props.length;
        this.checked = true;
    }
}

/**
 * PageInfoDto 的 JavaScript 类
 */
class PageInfo {
    constructor(props) {
        if (props == null) {
            return;
        }

        this.rowCount = props.rowCount;
        this.totalCount = props.totalCount;
        this.page = props.page;
        this.totalPage = props.totalPage;
    }
}

class DbfFields {
    constructor(props) {
        if (props == null) {
            return;
        }

        this.dataType = props.dataType;
        this.type = props.type;
        this.name = props.name;
    }
}

class DbfRowsDto {
    constructor(props) {
        if (props == null) {
            return;
        }

        this.id = props.id;
        this.name = props.name;
        this.dbfFields = buildDbfFields(props.dbfFields);
        this.rows = props.rows;
    }
}

function buildDbfFields(tmpDbfFields) {
    let list = [];
    if (tmpDbfFields != null && tmpDbfFields.length > 0) {
        for (let i = 0; i < tmpDbfFields.length; i++) {
            list.push(new DbfFields(tmpDbfFields[i]));
        }
    }
    return list;
}
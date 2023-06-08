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
let app = new Vue({
    el: '#app',
    data: {
        dbfRecordInfoDto: new DbfRecordInfoDto(),
        rowCount: 100,
        dbfRowsDto: new DbfRowsDto(),
        chooseAll: true,
        exportStart: 1,
        exportEnd: 100,
        status: "",
        deleteAfterUpload: true,
        expireTime: 60,
    },
    created() {
    },
    methods: {
        upload() {
            let url = "upload/dbf";
            let input = document.getElementById("uploadFileInput");
            const file = input.files[0];
            // 在这里进行一系列的校验
            const formData = new FormData();
            formData.append("file", file);
            formData.append("deleteAfterUpload", this.deleteAfterUpload);
            formData.append("expireTime", this.expireTime);

            this.status = "上传中，请稍后...";

            this.dbfRecordInfoDto = new DbfRecordInfoDto();
            axios.post(url, formData, {
                'Content-type': 'multipart/form-data'
            }).then(res => {
                this.status = "";
                let data = res.data.result;
                this.dbfRecordInfoDto = new DbfRecordInfoDto(data);
                this.exportEnd = this.dbfRecordInfoDto.recordNums;
            }, err => {
                // 出现错误时的处理
            });
        },
        parseTimeToStr(t) {
            return parseTimeToStr(t);
        },
        getRows() {
            let url = "dbf/getRows";
            let params = {params: {hexId: this.dbfRecordInfoDto.hexId, rowCount: this.rowCount}};
            axios.get(url, params).then((res) => {
                let data = res.data.result;
                let dbfRowsDto = new DbfRowsDto(data);
                this.dbfRowsDto = dbfRowsDto;
                console.log(dbfRowsDto);
            });
        },
        exportExcel() {
            let url = "dbf/exportToExcel";
            let params = {
                params: {
                    hexId: this.dbfRecordInfoDto.hexId,
                    colIndexes: null,
                    fileName: null,
                    exportStart: this.exportStart,
                    exportEnd: this.exportEnd,
                    chooseAll: this.chooseAll,
                }
            };
            axios.get(url, params).then((res) => {
                let data = res.data.result;
                if (data != null && data.length > 0) {
                    let downloadUrl = window.location.href + data;
                    console.log(downloadUrl);
                    window.open(downloadUrl, '_blank');
                }
            });
        },
    }
});
let app = new Vue({
    el: '#app',
    data: {
        dataId: "",
        dbfRecordInfoDto: new DbfRecordInfoDto(),
        dataSimpleInfoDto: new DataSimpleInfoDto(),
        dataDto: new DataDto(),
        rowCount: 100,
        page: 1,
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
            let url = "upload/file";
            let input = document.getElementById("uploadFileInput");
            const file = input.files[0];
            const formData = new FormData();
            formData.append("file", file);
            formData.append("deleteAfterUpload", this.deleteAfterUpload);
            formData.append("expireTime", this.expireTime);

            this.status = "上传中，请稍后...";

            this.dataSimpleInfoDto = new DataSimpleInfoDto();
            axios.post(url, formData, {
                'Content-type': 'multipart/form-data'
            }).then(res => {
                this.status = "";
                this.dataId = res.data.result;
                this.getHeads(this.dataId);
            }, err => {
                // 出现错误时的处理
            });
        },
        getHeads(dataId) {
            let url = "data/getHeads";
            axios.get(url, {params: {dataId: this.dataId}}).then((res) => {
                this.dataSimpleInfoDto = new DataSimpleInfoDto(res.data.result);
                this.exportEnd = this.dataSimpleInfoDto.recordNums;
            });
        },
        parseTimeToStr(t) {
            return parseTimeToStr(t);
        },
        changePage(pages) {
            this.page = pages + parseInt(this.page);
            if (this.page <= 0) {
                this.page = 1;
            }
            this.getDataList();
        },
        getDataList() {
            let url = "data/getDataList";
            let params = {params: {dataId: this.dataId, rowCount: this.rowCount, page: this.page}};
            axios.get(url, params).then((res) => {
                let data = res.data.result;
                this.dataDto = new DataDto(data);
                this.exportStart = (this.dataDto.pageInfo.page - 1) * this.dataDto.pageInfo.rowCount + 1;
                this.exportEnd = this.exportStart + this.dataDto.pageInfo.rowCount;
            });
        },
        exportExcel() {
            let url = "export/exportToExcel";
            let params = {
                params: {
                    dataId: this.dataId,
                    colIndexes: [],
                    fileName: null,
                    exportStart: this.exportStart,
                    exportEnd: this.exportEnd,
                    chooseAll: this.chooseAll,
                }
            };
            axios.get(url, params).then((res) => {
                let exportKey = res.data.result;
                if (exportKey != null && exportKey.length > 0) {

                    let downloadUrl = "download/downloadFile?exportKey=" + exportKey;
                    console.log(downloadUrl);
                    window.open(downloadUrl, '_blank');
                }
            });
        },
    }
});
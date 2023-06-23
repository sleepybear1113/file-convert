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
        expireTimeMinutes: 60,
        dataLoading: false,
        exportStatusList: [],
        exportButtonList: ["导出Excel", "导出Dbf"],
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
            formData.append("expireTimeMinutes", this.expireTimeMinutes);

            this.status = "上传中，请稍后...";

            this.dataSimpleInfoDto = new DataSimpleInfoDto();
            axios.post(url, formData, {
                'Content-type': 'multipart/form-data'
            }).then(res => {
                this.status = "";
                this.dataId = res.data.result;
                this.getHeads(this.dataId);
                this.getDataList();
            }, err => {
                // 出现错误时的处理
            });
        },
        getHeads(dataId) {
            let url = "data/getHeads";
            axios.get(url, {params: {dataId: this.dataId}}).then((res) => {
                this.dataSimpleInfoDto = new DataSimpleInfoDto(res.data.result);
                this.exportEnd = this.dataSimpleInfoDto.recordNums;
                this.dataDto = new DataDto();
            });
        },
        parseTimeToStr(t) {
            return parseTimeToStr(t);
        },
        changePage(pages) {
            if (pages === 0) {
                this.page = 1;
            }
            this.page = pages + parseInt(this.page);
            if (this.page <= 0) {
                this.page = 1;
            }
            this.getDataList();
        },
        getDataList() {
            let url = "data/getDataList";
            let params = {params: {dataId: this.dataId, rowCount: this.rowCount, page: this.page}};
            this.dataLoading = true;
            axios.get(url, params).then((res) => {
                let data = res.data.result;
                this.dataDto = new DataDto(data);
                this.exportStart = (this.dataDto.pageInfo.page - 1) * this.dataDto.pageInfo.rowCount + 1;
                this.exportEnd = this.exportStart + this.dataDto.pageInfo.rowCount;
                this.dataSimpleInfoDto = new DataSimpleInfoDto(this.dataDto);
                this.dataSimpleInfoDto.recordNums = this.dataDto.pageInfo.totalCount;
                this.dataLoading = false;
            }).catch((err) => {
                this.dataLoading = false;
            });
        },
        changeExportStatus(index, status) {
            this.exportStatusList[index] = status;
            this.$forceUpdate();
        },
        exportFile(type) {
            this.changeExportStatus(type, true);
            if (type === 0) {
                this.exportExcel(type);
            } else if (type === 1) {
                this.exportDbf(type);
            }
        },
        exportExcel(type) {
            let url = "export/exportToExcel";
            this.exportToFile(url, type);
        },
        exportDbf(type) {
            let url = "export/exportToDbf";
            this.exportToFile(url, type);
        },
        exportToFile(url, type) {
            let params = {
                params: {
                    dataId: this.dataId,
                    colIndexes: this.getSelectedHeadIndexes().join(","),
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
                this.changeExportStatus(type, false);
            }).catch((err) => {
                this.changeExportStatus(type, false);
            });
        },
        getSelectedHeadIndexes() {
            let result = [];
            if (this.dataDto && this.dataDto.heads) {
                for (let i = 0; i < this.dataDto.heads.length; i++) {
                    if (this.dataDto.heads[i].checked) {
                        result.push(i);
                    }
                }
            }
            return result;
        }
    }
});
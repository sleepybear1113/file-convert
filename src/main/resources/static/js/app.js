let app = new Vue({
    el: '#app',
    data: {
        acceptFileTypes: ".xls,.xlsx,.dbf,.csv,.sql,.db,.sqlite",
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
        fileUploading: false,
        dataLoading: false,
        exportStatusList: [],
        exportButtonList: ["导出Excel", "导出Dbf"],
        exportZipButtonList: ["导出Excel分组压缩包", "导出Dbf分组压缩包"],
        enableSelectedIndexes: false,
        enableGroupByIndexes: false,
    },
    created() {
    },
    methods: {
        upload() {
            let url = "/upload/file";
            let input = document.getElementById("uploadFileInput");
            const file = input.files[0];
            const formData = new FormData();
            formData.append("file", file);
            formData.append("deleteAfterUpload", this.deleteAfterUpload);
            formData.append("expireTimeMinutes", this.expireTimeMinutes);

            this.status = "上传中，请稍后...";
            this.fileUploading = true;

            this.dataSimpleInfoDto = new DataSimpleInfoDto();
            axios.post(url, formData, {
                'Content-type': 'multipart/form-data'
            }).then(res => {
                this.status = "";
                this.dataId = res.data.result;
                this.getHeads(this.dataId);
                this.getDataList();
                this.fileUploading = false;
            }, err => {
                // 出现错误时的处理
                this.fileUploading = false;
            });
        },
        getHeads(dataId) {
            let url = "/data/getHeads";
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
            let url = "/data/getDataList";
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
        deleteByDataId() {
            let url = "/data/deleteByDataId";
            let params = {params: {dataId: this.dataId}};
            axios.get(url, params).then((res) => {
                let data = res.data.result;
                this.dataId = "";
                this.dataDto = new DataDto();
                this.dataSimpleInfoDto = new DataSimpleInfoDto();
                if (data) {
                    alert("删除成功！");
                } else {
                    alert("删除失败！找不到该数据或者已经删除！");
                }
            });
        },
        changeExportStatus(index, status) {
            this.exportStatusList[index] = status;
            this.$forceUpdate();
        },
        exportFile(type) {
            if (!this.dataDto) {
                return;
            }
            if (this.enableGroupByIndexes) {
                let list = this.boolToIndexList(this.dataDto.groupByIndexes);
                if (list.length === 0) {
                    alert("分组导出时，分组列不能为空！");
                    return;
                }
            }
            this.changeExportStatus(type, true);
            if (type === 0) {
                this.exportExcel(type);
            } else if (type === 1) {
                this.exportDbf(type);
            }
        },
        exportExcel(type) {
            let url = "/export/exportToExcel";
            this.exportToFile(url, type);
        },
        exportDbf(type) {
            let url = "/export/exportToDbf";
            this.exportToFile(url, type);
        },
        exportToFile(url, type) {
            let params = {
                params: {
                    dataId: this.dataId,
                    colIndexes: this.boolToIndexList(this.dataDto.selectedIndexes).join(","),
                    groupByIndexes: this.boolToIndexList(this.dataDto.groupByIndexes).join(","),
                    fileName: null,
                    exportStart: this.exportStart,
                    exportEnd: this.exportEnd,
                    chooseAll: this.chooseAll,
                }
            };
            axios.get(url, params).then((res) => {
                let exportKey = res.data.result;
                if (exportKey != null && exportKey.length > 0) {

                    let downloadUrl = axios.defaults.baseURL + "/download/downloadFile?exportKey=" + exportKey;
                    console.log(downloadUrl);
                    window.open(downloadUrl, '_blank');
                }
                this.changeExportStatus(type, false);
            }).catch((err) => {
                this.changeExportStatus(type, false);
            });
        },
        boolToIndexList(boolList) {
            let result = [];
            for (let i = 0; i < boolList.length; i++) {
                if (boolList[i]) {
                    result.push(i);
                }
            }
            return result;
        },
        enableSelectedChange() {
            for (let i = 0; i < this.dataDto.selectedIndexes.length; i++) {
                this.dataDto.selectedIndexes[i] = !this.enableSelectedIndexes;
            }
        },
        enableGroupByChange() {
            for (let i = 0; i < this.dataDto.groupByIndexes.length; i++) {
                this.dataDto.groupByIndexes[i] = !!this.enableGroupByIndexes;
            }
        },
    }
});
let app = new Vue({
    el: '#app',
    data: {
        version: "",
        downloadPrefix: axios.defaults.baseURL + "/download/downloadFile?exportKey=",
        acceptFileTypes: ".xls,.xlsx,.dbf,.csv,.sql,.db,.sqlite",
        selectedFileName: "",
        dataId: "",
        dataDto: new DataDto(),
        rowCount: 100,
        page: 1,
        chooseAll: true,
        exportStart: 1,
        exportEnd: 100,
        deleteAfterUpload: true,
        expireTimeMinutes: 60,
        fileUploading: false,
        dataLoading: false,
        exporting: false,
        toastMsg: "",
        exportButtonList: [
            new ButtonExport("导出Excel", "/export/exportToExcel"),
            new ButtonExport("导出CSV", "/export/exportToCsv"),
            new ButtonExport("导出DBF", "/export/exportToDbf"),
        ],
        exportZipButtonList: [
            new ButtonExport("导出Excel分组压缩包", "/export/exportToExcel"),
            new ButtonExport("导出CSV分组压缩包", "/export/exportToCsv"),
            new ButtonExport("导出DBF分组压缩包", "/export/exportToDbf"),
        ],
        enableSelectedIndexes: false,
        enableGroupByIndexes: false,
        exportKey: "",
        confirmData: {
            title: "",
            content: "",
            action: "",
            data: {},
        }
    },
    created() {
        this.getVersion();
    },
    methods: {
        getVersion() {
            let url = "system/getVersion";
            axios.get(url).then(res => {
                this.version = res.data.result;
            }).catch(() => {
            });
        },
        clear() {
            this.dataId = "";
            this.dataDto = new DataDto();
            this.chooseAll = true;
            this.deleteAfterUpload = true;
            this.fileUploading = false;
            this.dataLoading = false;
            this.exporting = false;
            this.enableSelectedIndexes = false;
            this.enableGroupByIndexes = false;
            this.exportKey = "";
        },
        changeFile(event) {
            let fileInput = event.target;
            let selectedFiles = fileInput.files;
            if (!selectedFiles || selectedFiles.length === 0) {
                this.selectedFileName = "";
                return;
            }
            let selectedFile = selectedFiles[0];
            this.selectedFileName = selectedFile.name;
        },
        upload() {
            let url = "/upload/file";
            let input = document.getElementById("uploadFileInput");
            const file = input.files[0];
            if (!file) {
                showAlertWarning("请选择文件");
                return;
            }
            const formData = new FormData();
            formData.append("file", file);
            formData.append("deleteAfterUpload", this.deleteAfterUpload);
            formData.append("expireTimeMinutes", this.expireTimeMinutes);

            this.clear();
            this.fileUploading = true;
            this.exportKey = "";

            axios.post(url, formData, {
                'Content-type': 'multipart/form-data'
            }).then(res => {
                this.dataId = res.data.result;
                this.getDataList(1);
                this.fileUploading = false;
            }).catch(err => {
                // 出现错误时的处理
                showAlertWarning("上传失败，请选择其他文件");
                this.fileUploading = false;
            });
        },
        getHeads(dataId) {
            let url = "/data/getHeads";
            axios.get(url, {params: {dataId: this.dataId}}).then((res) => {
                this.dataDto = new DataDto(res.data.result);
                this.exportEnd = this.dataDto.recordNums;
            });
        },
        parseTimeToStr(t) {
            return parseTimeToStr(t);
        },
        validPage() {
            if (this.page <= 0) {
                this.page = 1;
                this.showToast("已经是第一页了！");
                return;
            }
            if (this.page > this.dataDto.pageInfo.totalPage) {
                this.page = this.dataDto.pageInfo.totalPage;
                this.showToast("已经是最后一页了！");
            }
        },
        changePage(pages) {
            if (pages === 0) {
                this.page = 1;
            }
            this.page = pages + parseInt(this.page);
            this.validPage();
            this.getDataList(this.page);
        },
        getDataList(page) {
            let url = "/data/getDataList";
            this.page = page;
            this.validPage();

            let params = {params: {dataId: this.dataId, rowCount: this.rowCount, page: this.page}};
            this.dataLoading = true;
            this.exportKey = "";
            axios.get(url, params).then((res) => {
                this.clear();
                let data = res.data.result;
                this.dataDto = new DataDto(data);
                this.dataId = this.dataDto.id;
                this.exportStart = (this.dataDto.pageInfo.page - 1) * this.dataDto.pageInfo.rowCount + 1;
                this.exportEnd = this.exportStart + this.dataDto.pageInfo.rowCount;
                this.dataDto.recordNums = this.dataDto.pageInfo.totalCount;
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
                if (data) {
                    showAlertSuccess("删除成功！");
                } else {
                    showAlertWarning("删除失败！找不到该数据或者已经删除！");
                }
            });
        },
        deleteDownloadFile(exportKey) {
            let url = "/export/deleteDownloadFile";
            let params = {params: {downloadId: exportKey}};
            axios.get(url, params).then((res) => {
                let data = res.data.result;
                if (data) {
                    showAlertSuccess("删除成功！");
                }
                this.exportKey = "";
            }).catch((err) => {
                this.exportKey = "";
            });
        },
        preProcessExport(exportUrl) {
            if (!this.dataDto) {
                return;
            }
            if (this.enableGroupByIndexes) {
                let list = this.boolToIndexList(this.dataDto.heads, "groupByChecked");
                if (list.length === 0) {
                    showAlertWarning("分组导出时，分组列不能为空！");
                    return;
                }
            }

            this.exporting = true;
            let url = "/export/preProcessExport";
            let params = {
                params: {
                    dataId: this.dataId,
                    colIndexes: this.boolToIndexList(this.dataDto.heads, "shown").join(","),
                    groupByIndexes: this.boolToIndexList(this.dataDto.heads, "groupByChecked").join(","),
                    fileName: null,
                    exportStart: this.exportStart,
                    exportEnd: this.exportEnd,
                    chooseAll: this.chooseAll,
                }
            };
            axios.get(url, params).then((res) => {
                let batchDownloadInfoDto = new BatchDownloadInfoDto(res.data.result);
                if (!batchDownloadInfoDto || batchDownloadInfoDto.dataDtoCount === 0) {
                    showAlertWarning("导出失败！没有数据！");
                    this.exporting = false;
                    return;
                }

                this.exportKey = "";
                let dataDtoCount = batchDownloadInfoDto.dataDtoCount;
                let totalDataCount = batchDownloadInfoDto.totalDataCount;
                let id = batchDownloadInfoDto.id;
                let directExport = false;
                if (dataDtoCount <= 10) {
                    directExport = true;
                } else if (dataDtoCount <= 30 && dataDtoCount * 4 >= totalDataCount) {
                    directExport = true;
                } else if (dataDtoCount <= 200 && dataDtoCount * 15 >= totalDataCount) {
                    directExport = true;
                } else if (dataDtoCount > 400) {
                    showAlertWarning("预处理完成，但是等待生成和压缩的文件有" + dataDtoCount + "个，导出文件过多，系统处理能力有限，暂时无法导出！");
                    this.exporting = false;
                    return;
                }
                if (!directExport) {
                    let content = `预处理完成，但是等待生成和压缩的文件有${dataDtoCount}个，而总数据条数有${totalDataCount}条。若选择继续导出，可能会等待较长时间，是否继续？`;
                    this.showConfirm("提示", content, "exportMoreToFile", {exportUrl: exportUrl, id: id});
                } else {
                    this.exportToFile(exportUrl, id);
                }
            }).catch((err) => {
                this.exporting = false;
            });
        },
        exportToFile(url, batchDownloadInfoId) {
            let params = {
                params: {
                    batchDownloadInfoId: batchDownloadInfoId,
                }
            };
            axios.get(url, params).then((res) => {
                let exportKey = res.data.result;
                if (exportKey != null && exportKey.length > 0) {
                    let downloadUrl = this.downloadPrefix + exportKey;
                    this.exportKey = exportKey;
                    // document.getElementById("download-ele-a").click();
                    this.downloadUrl(downloadUrl);
                }
                this.exporting = false;
            }).catch((err) => {
                this.exporting = false;
            });
        },
        boolToIndexList(boolList, colName) {
            let result = [];
            for (let i = 0; i < boolList.length; i++) {
                if (boolList[i][colName]) {
                    result.push(i);
                }
            }
            return result;
        },
        enableGroupByChange() {
            for (let i = 0; i < this.dataDto.heads.length; i++) {
                Vue.set(this.dataDto.heads[i], "groupByChecked", false);
            }
        },
        changeColDisplay(bool) {
            let heads = this.dataDto.heads;
            if (!heads) {
                return;
            }
            for (let i = 0; i < heads.length; i++) {
                if (bool === true) {
                    Vue.set(heads[i], "shown", true);
                } else if (bool === false) {
                    Vue.set(heads[i], "shown", false);
                } else {
                    Vue.set(heads[i], "shown", !heads[i].shown);
                }
            }
        },
        initTooltip() {
            setTimeout(() => {
                const tooltipTriggerList = document.querySelectorAll('.x-tooltip');
                const tooltipList = [...tooltipTriggerList].map(tooltipTriggerEl => new bootstrap.Tooltip(tooltipTriggerEl));
            }, 300);
            return true;
        },
        showToast(toastMsg) {
            this.toastMsg = toastMsg;
            let toastLiveExample = document.getElementById('liveToast');
            let toast = new bootstrap.Toast(toastLiveExample);
            toast.show();
        },
        showConfirm(title, content, action, data) {
            this.confirmData.title = title;
            this.confirmData.content = content;
            this.confirmData.action = action;
            this.confirmData.data = data;

            let confirmModal = new bootstrap.Modal(document.getElementById('confirmModal'));
            confirmModal.show();
        },
        confirmAction(action, bool, data) {
            switch (action) {
                case "exportMoreToFile":
                    this.exportMoreToFile(bool, data);
                    break;
            }
        },
        exportMoreToFile(bool, data) {
            if (!bool) {
                this.exporting = false;
                return;
            }

            this.exportToFile(data["exportUrl"], data["id"]);
        },
        downloadUrl(url) {
            const link = document.createElement('a');
            link.href = url;
            link.style.display = 'none';
            document.body.appendChild(link);
            link.click();
            document.body.removeChild(link);
        },
    }
});
let app = new Vue({
    el: '#app',
    data: {
        version: "",
        inputFullId: "",
        downloadPrefix: axios.defaults.baseURL + "/download/downloadFile?exportKey=",
        acceptFileTypes: ".xls,.xlsx,.dbf,.csv,.sql,.db,.sqlite,.zip",
        selectedFileName: "",
        uploadFileInfoDto: new UploadFileInfoDto(),
        totalUploadFileInfoDto: {},
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
        enableExportZip: false,
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
        clear(clearUploadFileInfo = false) {
            if (clearUploadFileInfo) {
                this.totalUploadFileInfoDto = {};
                this.changeCurrentUploadFileInfo(null);
            }
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
        createInputUpload(inputId) {
            let parentDiv = document.getElementById("input-div");
            while (parentDiv.firstChild) {
                parentDiv.removeChild(parentDiv.firstChild);
            }

            let inputElement = document.createElement("input");
            inputElement.setAttribute("type", "file");
            inputElement.setAttribute("accept", this.acceptFileTypes);
            inputElement.addEventListener('change', (event) => {
                this.changeFile(event);
            });
            parentDiv.appendChild(inputElement);
            inputElement.click();
        },
        changeFile(event) {
            let selectedFiles;
            if (event.type === 'drop') {
                selectedFiles = event.dataTransfer.files;
            } else if (event.type === 'change') {
                selectedFiles = event.target.files;
            }

            if (!selectedFiles || selectedFiles.length === 0) {
                this.selectedFileName = "";
                return;
            }
            let selectedFile = selectedFiles[0];
            this.selectedFileName = selectedFile.name;

            this.uploadFile(selectedFile);
        },
        uploadFile(selectedFile) {
            if (!selectedFile) {
                showAlertWarning("请选择文件！");
                return;
            }
            let url = "/upload/file";
            const formData = new FormData();
            formData.append("file", selectedFile);
            formData.append("deleteAfterUpload", this.deleteAfterUpload);
            formData.append("expireTimeMinutes", this.expireTimeMinutes);

            this.clear(true);
            this.fileUploading = true;
            this.exportKey = "";

            axios.post(url, formData, {
                'Content-type': 'multipart/form-data'
            }).then(res => {
                this.totalUploadFileInfoDto = new TotalUploadFileInfoDto(res.data.result);
                if (this.totalUploadFileInfoDto.uploadFileInfoDtoList && this.totalUploadFileInfoDto.uploadFileInfoDtoList.length > 0) {
                    this.changeCurrentUploadFileInfo(this.totalUploadFileInfoDto.uploadFileInfoDtoList[0])
                }
                this.getDataList(1);
                this.fileUploading = false;
                if (this.totalUploadFileInfoDto.uploadFileInfoDtoList && this.totalUploadFileInfoDto.uploadFileInfoDtoList.length > 1) {
                    showAlertSuccess("上传成功，共" + this.totalUploadFileInfoDto.uploadFileInfoDtoList.length + "个文件");
                }
            }).catch(err => {
                // 出现错误时的处理
                showAlertWarning("上传失败，请选择其他文件！" + err.data.message);
                this.fileUploading = false;
            });
        },
        getUploadFileInfoDto() {
            let url = "/data/getUploadFileInfoDto";
            axios.get(url, {params: {id: this.inputFullId}}).then((res) => {
                this.totalUploadFileInfoDto = new TotalUploadFileInfoDto(res.data.result);
            });
        },
        changeCurrentUploadFileInfo(uploadFileInfoDto) {
            if (!uploadFileInfoDto) {
                this.uploadFileInfoDto = new UploadFileInfoDto();
            } else {
                this.uploadFileInfoDto = uploadFileInfoDto;
            }
            this.inputFullId = this.uploadFileInfoDto.getFullId();
        },
        getHeads(dataId) {
            let url = "/data/getHeads";
            axios.get(url, {params: {id: this.inputFullId}}).then((res) => {
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
        getDataList(page, newFetch = false) {
            if (!this.inputFullId) {
                return;
            }
            let url = "/data/getDataList";
            this.page = page;
            this.validPage();

            let params = {params: {id: this.inputFullId, rowCount: this.rowCount, page: this.page}};
            this.dataLoading = true;
            this.exportKey = "";
            axios.get(url, params).then((res) => {
                this.clear();
                let data = res.data.result;
                this.dataDto = new DataDto(data);
                this.exportStart = (this.dataDto.pageInfo.page - 1) * this.dataDto.pageInfo.rowCount + 1;
                this.exportEnd = this.exportStart + this.dataDto.pageInfo.rowCount - 1;
                this.dataDto.recordNums = this.dataDto.pageInfo.totalCount;
                this.dataLoading = false;

                this.changeCurrentUploadFileInfo(this.dataDto.getUploadFileInfoDto());

                if (newFetch) {
                    this.getUploadFileInfoDto();
                }
            }).catch((err) => {
                this.dataLoading = false;
            });
        },
        changeFileData(info) {
            this.changeCurrentUploadFileInfo(info);
            this.getDataList(1);
        },
        deleteByDataId() {
            let url = "/data/deleteByDataId";
            let params = {params: {id: this.inputFullId}};
            axios.get(url, params).then((res) => {
                let data = res.data.result;
                if (data) {
                    showAlertSuccess("删除成功！");
                } else {
                    showAlertWarning("删除失败！找不到该数据或者已经删除！");
                }

                let arr = this.totalUploadFileInfoDto.uploadFileInfoDtoList;
                if (arr && arr.length > 0) {
                    for (let i = 0; i < arr.length; i++) {
                        if (arr[i].dataId === this.uploadFileInfoDto.dataId) {
                            Vue.slice(arr, i, 1);
                            break;
                        }
                    }
                }

                this.changeCurrentUploadFileInfo(null);
                this.dataDto = new DataDto();
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

            let dataIdList = this.inputFullId;
            if (this.enableExportZip) {
                if (this.totalUploadFileInfoDto.uploadFileInfoDtoList && this.totalUploadFileInfoDto.uploadFileInfoDtoList.length > 0) {
                    let uploadFileInfoDtoList = this.totalUploadFileInfoDto.uploadFileInfoDtoList;
                    let dataListArr = [];
                    for (let i = 0; i < uploadFileInfoDtoList.length; i++) {
                        let item = uploadFileInfoDtoList[i];
                        if (item.checked) {
                            dataListArr.push(item.getFullId());
                        }
                    }
                    dataIdList = dataListArr.join(",");
                }
            }
            let params = {
                params: {
                    dataIdList: dataIdList,
                    colIndexes: this.enableExportZip ? "" : this.boolToIndexList(this.dataDto.heads, "shown").join(","),
                    groupByIndexes: this.enableExportZip ? "" : this.boolToIndexList(this.dataDto.heads, "groupByChecked").join(","),
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
        boolToIndexList(boolList, colName, targetName = null) {
            let result = [];
            for (let i = 0; i < boolList.length; i++) {
                if (boolList[i][colName]) {
                    if (!targetName) {
                        result.push(i);
                    } else {
                        result.push(boolList[i][targetName])
                    }
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
        handleFileSelect(event) {
            event.preventDefault();
            this.changeFile(event);
            this.resetStyle();
        },
        dragOverHandler(event) {
            event.preventDefault();
        },
        dragEnterHandler(event) {
            event.preventDefault();
            document.getElementById('fileDropArea').classList.add('drag');
        },
        dragLeaveHandler(event) {
            event.preventDefault();
            this.resetStyle();
        },
        resetStyle() {
            document.getElementById('fileDropArea').classList.remove('drag');
        },
    }
});
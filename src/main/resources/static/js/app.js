let app = new Vue({
    el: "#app",
    data: {
        toastMsg: "",
        downloadPrefix: axios.defaults.baseURL + "/download/downloadFile?exportKey=",
        acceptFileTypes: ".xls,.xlsx,.dbf,.csv,.sql,.db,.sqlite,.zip",

        basicInfo: new BasicInfoDto().default(),
        basicSetting: new BasicSetting().default(),
        uploadingStatus: new UploadingStatus().default(),
        fileStatus: new FileStatus().default(),
        pageInfo: new PageInfo().default(),
        dragMaskStatus: new DragMaskStatus().default(),
        exportStatus: new ExportStatus().default(),
        confirmData: new ConfirmData().default(),

        uploadFileInfoDto: new UploadFileInfoDto(),
        totalUploadFileInfoDto: new TotalUploadFileInfoDto(),
        dataDto: new DataDto(),

        exportButtonList: new ButtonExport().buildList([["导出Excel", "/export/exportToExcel"], ["导出CSV", "/export/exportToCsv"], ["导出DBF", "/export/exportToDbf"]]),
        exportZipButtonList: new ButtonExport().buildList([["导出Excel分组压缩包", "/export/exportToExcel"], ["导出CSV分组压缩包", "/export/exportToCsv"], ["导出DBF分组压缩包", "/export/exportToDbf"]]),
    },
    created() {
        this.getVersion();

        this.loadBasicSettingFromLocalStorage();
    },
    methods: {
        getVersion() {
            let url = "/system/getBasicInfoDto";
            axios.get(url).then(res => {
                this.basicInfo = new BasicInfoDto(res.data.result);
            }).catch(() => {
            });
        },
        clear(clearUploadFileInfo = false) {
            if (clearUploadFileInfo) {
                this.totalUploadFileInfoDto = {};
                this.changeCurrentUploadFileInfo(null);
            }
            this.dataDto = new DataDto();
            this.exportStatus.chooseAll = true;
            this.basicSetting.deleteAfterUpload = true;
            this.uploadingStatus.fileUploading = false;
            this.pageInfo.dataLoading = false;
            this.exportStatus.exporting = false;
            this.exportStatus.enableGroupByIndexes = false;
            this.exportStatus.exportKey = "";
        },
        /**
         * 创建上传文件的 input 元素，隐藏不显示
         */
        createInputUpload() {
            let parentDiv = document.getElementById("input-div");
            while (parentDiv.firstChild) {
                parentDiv.removeChild(parentDiv.firstChild);
            }

            let inputElement = document.createElement("input");
            inputElement.setAttribute("type", "file");
            inputElement.setAttribute("accept", this.acceptFileTypes);
            inputElement.addEventListener("change", (event) => this.changeFile(event));
            parentDiv.appendChild(inputElement);
            inputElement.click();
        },
        changeFile(event) {
            let selectedFiles;
            if (event.type === "drop") {
                selectedFiles = event.dataTransfer.files;
            } else if (event.type === "change") {
                selectedFiles = event.target.files;
            }

            if (!selectedFiles || selectedFiles.length === 0) {
                this.fileStatus.selectedFileName = "";
                return;
            }
            let selectedFile = selectedFiles[0];
            let b = this.uploadFile(selectedFile);
            if (b) {
                this.fileStatus.selectedFileName = selectedFile.name;
            }
        },
        uploadFile(selectedFile) {
            if (!selectedFile) {
                showAlertWarning("请选择文件！");
                return false;
            }

            if (selectedFile.size > this.basicInfo.acceptMaxFileSize) {
                showAlertWarning(`文件大小过大，无法上传！限制大小为${parseFileSize(this.basicInfo.acceptMaxFileSize)}！目前文件大小为${parseFileSize(selectedFile.size)}`);
                return false;
            }

            if (this.uploadingStatus.fileUploading) {
                showAlertWarning("目前正在有文件上传中，请等待！");
                return false;
            }

            let url = "/upload/file";
            const formData = new FormData();
            formData.append("file", selectedFile);
            formData.append("deleteAfterUpload", this.basicSetting.deleteAfterUpload);
            formData.append("expireTimeMinutes", this.basicSetting.expireTimeMinutes);
            formData.append("id", this.basicSetting.multiFileMerge ? this.fileStatus.inputFullId : "");

            this.clear(true);
            this.uploadingStatus.fileUploading = true;
            this.exportStatus.exportKey = "";

            this.uploadingStatus.fileUploadPercent = 0;
            axios.post(url, formData, {
                "Content-type": "multipart/form-data",
                onUploadProgress: (progressEvent) => {
                    this.uploadingStatus.fileUploadPercent = ((progressEvent.loaded * 100) / progressEvent.total).toFixed(2);
                }
            }).then(res => {
                this.totalUploadFileInfoDto = new TotalUploadFileInfoDto(res.data.result);
                if (this.totalUploadFileInfoDto.uploadFileInfoDtoList && this.totalUploadFileInfoDto.uploadFileInfoDtoList.length > 0) {
                    this.changeCurrentUploadFileInfo(this.totalUploadFileInfoDto.uploadFileInfoDtoList[0])
                }
                this.getDataList(1);
                this.uploadingStatus.fileUploading = false;
                if (this.totalUploadFileInfoDto.uploadFileInfoDtoList && this.totalUploadFileInfoDto.uploadFileInfoDtoList.length > 1) {
                    if (this.totalUploadFileInfoDto.newlyAddedCount) {
                        if (this.totalUploadFileInfoDto.newlyAddedCount > 1) {
                            showAlertSuccess("上传成功，共" + this.totalUploadFileInfoDto.newlyAddedCount + "个文件");
                        }
                    } else {
                        showAlertSuccess("上传成功，共" + this.totalUploadFileInfoDto.uploadFileInfoDtoList.length + "个文件");
                    }
                }
                this.uploadingStatus.fileUploadPercent = 0;
            }).catch(err => {
                // 出现错误时的处理
                showAlertWarning("上传失败，请选择其他文件！" + err.data.message);
                this.uploadingStatus.fileUploading = false;
            });
            return true;
        },
        getUploadFileInfoDto() {
            let url = "/data/getUploadFileInfoDto";
            axios.get(url, {params: {id: this.fileStatus.inputFullId}}).then((res) => {
                this.totalUploadFileInfoDto = new TotalUploadFileInfoDto(res.data.result);
                if (this.totalUploadFileInfoDto.uploadFileInfoDtoList && this.totalUploadFileInfoDto.uploadFileInfoDtoList.length) {
                    this.totalUploadFileInfoDto.uploadFileInfoDtoList.forEach((item) => {
                        item.dataChecked = item.dataId === this.uploadFileInfoDto.dataId;
                    });
                }
            });
        },
        changeCurrentUploadFileInfo(uploadFileInfoDto) {
            if (!uploadFileInfoDto) {
                this.uploadFileInfoDto = new UploadFileInfoDto();
            } else {
                if (this.totalUploadFileInfoDto.uploadFileInfoDtoList) {
                    this.totalUploadFileInfoDto.uploadFileInfoDtoList.forEach((item) => {
                        item.dataChecked = item.dataId === uploadFileInfoDto.dataId;
                    });
                }

                this.uploadFileInfoDto = uploadFileInfoDto;
            }
            this.fileStatus.inputFullId = this.uploadFileInfoDto.getFullId();
        },
        getHeads(dataId) {
            let url = "/data/getHeads";
            axios.get(url, {params: {id: this.fileStatus.inputFullId}}).then((res) => {
                this.dataDto = new DataDto(res.data.result);
                this.exportStatus.exportEnd = this.dataDto.recordNums;
            });
        },
        parseTimeToStr(t) {
            return parseTimeToStr(t);
        },
        validPage() {
            if (this.pageInfo.page <= 0) {
                this.pageInfo.page = 1;
                this.showToast("已经是第一页了！");
                return;
            }
            if (this.pageInfo.page > this.dataDto.pageInfo.totalPage) {
                this.pageInfo.page = this.dataDto.pageInfo.totalPage;
                this.showToast("已经是最后一页了！");
            }
        },
        changePage(pages) {
            if (pages === 0) {
                this.pageInfo.page = 1;
            }
            this.pageInfo.page = pages + parseInt(this.pageInfo.page);
            this.validPage();
            this.getDataList(this.pageInfo.page);
        },
        getDataList(page, newFetch = false) {
            if (!this.fileStatus.inputFullId) {
                return;
            }
            let url = "/data/getDataList";
            this.pageInfo.page = page;
            this.validPage();

            let params = {
                params: {
                    id: this.fileStatus.inputFullId,
                    rowCount: this.pageInfo.rowCount,
                    page: this.pageInfo.page
                }
            };
            this.pageInfo.dataLoading = true;
            this.exportStatus.exportKey = "";
            axios.get(url, params).then((res) => {
                this.clear();
                let data = res.data.result;
                this.dataDto = new DataDto(data);
                this.exportStatus.exportStart = (this.dataDto.pageInfo.page - 1) * this.dataDto.pageInfo.rowCount + 1;
                this.exportStatus.exportEnd = this.exportStatus.exportStart + this.dataDto.pageInfo.rowCount - 1;
                this.dataDto.recordNums = this.dataDto.pageInfo.totalCount;
                this.pageInfo.dataLoading = false;

                this.changeCurrentUploadFileInfo(this.dataDto.getUploadFileInfoDto());

                if (newFetch) {
                    this.getUploadFileInfoDto();
                }
            }).catch((err) => {
                this.pageInfo.dataLoading = false;
            });
        },
        changeFileData(info) {
            this.changeCurrentUploadFileInfo(info);
            this.getDataList(1);
        },
        deleteByDataId() {
            let url = "/data/deleteByDataId";
            let params = {params: {id: this.fileStatus.inputFullId}};
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
                this.exportStatus.exportKey = "";
            }).catch((err) => {
            });
        },
        preProcessExport(exportUrl) {
            if (!this.dataDto) {
                return;
            }
            if (this.exportStatus.enableGroupByIndexes) {
                let list = this.boolToIndexList(this.dataDto.heads, "groupByChecked");
                if (list.length === 0) {
                    showAlertWarning("分组导出时，分组列不能为空！");
                    return;
                }
            }

            this.exportStatus.exporting = true;
            let url = "/export/preProcessExport";

            let dataIdList = this.fileStatus.inputFullId;
            if (this.exportStatus.enableExportZip || this.exportStatus.mergeDataList) {
                if (this.totalUploadFileInfoDto.uploadFileInfoDtoList && this.totalUploadFileInfoDto.uploadFileInfoDtoList.length > 0) {
                    let uploadFileInfoDtoList = this.totalUploadFileInfoDto.uploadFileInfoDtoList;
                    let dataListArr = [];
                    for (let i = 0; i < uploadFileInfoDtoList.length; i++) {
                        let item = uploadFileInfoDtoList[i];
                        if (item.zipChecked) {
                            dataListArr.push(item.getFullId());
                        }
                    }
                    dataIdList = dataListArr.join(",");
                }
            }
            let params = {
                params: {
                    dataIdList: dataIdList,
                    colIndexes: this.exportStatus.enableExportZip ? "" : this.boolToIndexList(this.dataDto.heads, "shown").join(","),
                    groupByIndexes: this.exportStatus.enableExportZip ? "" : this.boolToIndexList(this.dataDto.heads, "groupByChecked").join(","),
                    fileName: null,
                    exportStart: this.exportStatus.exportStart,
                    exportEnd: this.exportStatus.exportEnd,
                    chooseAll: this.exportStatus.chooseAll,
                    mergeDataList: this.exportStatus.mergeDataList,
                }
            };
            axios.get(url, params).then((res) => {
                let batchDownloadInfoDto = new BatchDownloadInfoDto(res.data.result);
                if (!batchDownloadInfoDto || batchDownloadInfoDto.dataDtoCount === 0) {
                    showAlertWarning("导出失败！没有数据！");
                    this.exportStatus.exporting = false;
                    return;
                }

                this.exportStatus.exportKey = "";
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
                    this.exportStatus.exporting = false;
                    return;
                }
                if (!directExport) {
                    let content = `预处理完成，但是等待生成和压缩的文件有${dataDtoCount}个，而总数据条数有${totalDataCount}条。若选择继续导出，可能会等待较长时间，是否继续？`;
                    this.showConfirm("提示", content, "exportMoreToFile", {exportUrl: exportUrl, id: id});
                } else {
                    this.exportToFile(exportUrl, id);
                }
            }).catch((err) => {
                this.exportStatus.exporting = false;
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
                    this.exportStatus.exportKey = exportKey;
                    this.downloadUrl(downloadUrl);
                }
                this.exportStatus.exporting = false;
            }).catch((err) => {
                this.exportStatus.exporting = false;
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
                const tooltipTriggerList = document.querySelectorAll(".x-tooltip");
                const tooltipList = [...tooltipTriggerList].map(tooltipTriggerEl => new bootstrap.Tooltip(tooltipTriggerEl));
            }, 300);
            return true;
        },
        showToast(toastMsg) {
            this.toastMsg = toastMsg;
            let toastLiveExample = document.getElementById("liveToast");
            let toast = new bootstrap.Toast(toastLiveExample);
            toast.show();
        },
        showConfirm(title, content, action, data) {
            this.confirmData.title = title;
            this.confirmData.content = content;
            this.confirmData.action = action;
            this.confirmData.data = data;

            let confirmModal = new bootstrap.Modal(document.getElementById("confirmModal"));
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
                this.exportStatus.exporting = false;
                return;
            }

            this.exportToFile(data["exportUrl"], data["id"]);
        },
        downloadUrl(url) {
            const link = document.createElement("a");
            link.href = url;
            link.style.display = "none";
            document.body.appendChild(link);
            link.click();
            document.body.removeChild(link);
        },
        handleFileSelect(event) {
            event.preventDefault();
            this.changeFile(event);
            this.changeUploadMask(false);
            document.getElementById("main").classList.remove("dragging-over");
        },
        dragOverHandler(event) {
            event.preventDefault();
            event.stopPropagation();
            this.changeUploadMask(true);
        },
        dragEnterHandler(event) {
            event.preventDefault();
            this.changeUploadMask(true);
            document.getElementById("main").classList.add("dragging-over");
            this.dragMaskStatus.lastDragEnter = event.target;
        },
        dragLeaveHandler(event) {
            event.preventDefault();
            if (this.dragMaskStatus.lastDragEnter === event.target) {
                console.log("dragleave", event.target);
                document.getElementById("main").classList.remove("dragging-over");
                event.stopPropagation();
                event.preventDefault();
                this.changeUploadMask(false);
            }
        },
        changeUploadMask(b) {
            this.dragMaskStatus.showUploadMask = b;
        },
        saveBasicSettingToLocalStorage() {
            localStorage.setItem("file-convert-basicSetting", JSON.stringify(this.basicSetting));
        },
        loadBasicSettingFromLocalStorage() {
            let basicSetting = localStorage.getItem("file-convert-basicSetting");
            if (basicSetting) {
                this.basicSetting = new BasicSetting(JSON.parse(basicSetting))
            }
        },
    }
});
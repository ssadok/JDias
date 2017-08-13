import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

import { NgbActiveModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { JhiEventManager } from 'ng-jhipster';

import { HashTag } from './hash-tag.model';
import { HashTagPopupService } from './hash-tag-popup.service';
import { HashTagService } from './hash-tag.service';

@Component({
    selector: 'jhi-hash-tag-delete-dialog',
    templateUrl: './hash-tag-delete-dialog.component.html'
})
export class HashTagDeleteDialogComponent {

    hashTag: HashTag;

    constructor(
        private hashTagService: HashTagService,
        public activeModal: NgbActiveModal,
        private eventManager: JhiEventManager
    ) {
    }

    clear() {
        this.activeModal.dismiss('cancel');
    }

    confirmDelete(id: number) {
        this.hashTagService.delete(id).subscribe((response) => {
            this.eventManager.broadcast({
                name: 'hashTagListModification',
                content: 'Deleted an hashTag'
            });
            this.activeModal.dismiss(true);
        });
    }
}

@Component({
    selector: 'jhi-hash-tag-delete-popup',
    template: ''
})
export class HashTagDeletePopupComponent implements OnInit, OnDestroy {

    routeSub: any;

    constructor(
        private route: ActivatedRoute,
        private hashTagPopupService: HashTagPopupService
    ) {}

    ngOnInit() {
        this.routeSub = this.route.params.subscribe((params) => {
            this.hashTagPopupService
                .open(HashTagDeleteDialogComponent as Component, params['id']);
        });
    }

    ngOnDestroy() {
        this.routeSub.unsubscribe();
    }
}

import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Response } from '@angular/http';

import { Observable } from 'rxjs/Rx';
import { NgbActiveModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { EventManager, AlertService } from 'ng-jhipster';

import { Tag } from './tag.model';
import { TagPopupService } from './tag-popup.service';
import { TagService } from './tag.service';
import { Post, PostService } from '../post';
import { ResponseWrapper } from '../../shared';

@Component({
    selector: 'jhi-tag-dialog',
    templateUrl: './tag-dialog.component.html'
})
export class TagDialogComponent implements OnInit {

    tag: Tag;
    authorities: any[];
    isSaving: boolean;

    posts: Post[];

    constructor(
        public activeModal: NgbActiveModal,
        private alertService: AlertService,
        private tagService: TagService,
        private postService: PostService,
        private eventManager: EventManager
    ) {
    }

    ngOnInit() {
        this.isSaving = false;
        this.authorities = ['ROLE_USER', 'ROLE_ADMIN'];
        this.postService.query()
            .subscribe((res: ResponseWrapper) => { this.posts = res.json; }, (res: ResponseWrapper) => this.onError(res.json));
    }
    clear() {
        this.activeModal.dismiss('cancel');
    }

    save() {
        this.isSaving = true;
        if (this.tag.id !== undefined) {
            this.subscribeToSaveResponse(
                this.tagService.update(this.tag), false);
        } else {
            this.subscribeToSaveResponse(
                this.tagService.create(this.tag), true);
        }
    }

    private subscribeToSaveResponse(result: Observable<Tag>, isCreated: boolean) {
        result.subscribe((res: Tag) =>
            this.onSaveSuccess(res, isCreated), (res: Response) => this.onSaveError(res));
    }

    private onSaveSuccess(result: Tag, isCreated: boolean) {
        this.alertService.success(
            isCreated ? 'jDiasApp.tag.created'
            : 'jDiasApp.tag.updated',
            { param : result.id }, null);

        this.eventManager.broadcast({ name: 'tagListModification', content: 'OK'});
        this.isSaving = false;
        this.activeModal.dismiss(result);
    }

    private onSaveError(error) {
        try {
            error.json();
        } catch (exception) {
            error.message = error.text();
        }
        this.isSaving = false;
        this.onError(error);
    }

    private onError(error) {
        this.alertService.error(error.message, null, null);
    }

    trackPostById(index: number, item: Post) {
        return item.id;
    }
}

@Component({
    selector: 'jhi-tag-popup',
    template: ''
})
export class TagPopupComponent implements OnInit, OnDestroy {

    modalRef: NgbModalRef;
    routeSub: any;

    constructor(
        private route: ActivatedRoute,
        private tagPopupService: TagPopupService
    ) {}

    ngOnInit() {
        this.routeSub = this.route.params.subscribe((params) => {
            if ( params['id'] ) {
                this.modalRef = this.tagPopupService
                    .open(TagDialogComponent, params['id']);
            } else {
                this.modalRef = this.tagPopupService
                    .open(TagDialogComponent);
            }
        });
    }

    ngOnDestroy() {
        this.routeSub.unsubscribe();
    }
}

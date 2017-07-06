import { Injectable, Component } from '@angular/core';
import { Router } from '@angular/router';
import { NgbModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { AspectVisibility } from './aspect-visibility.model';
import { AspectVisibilityService } from './aspect-visibility.service';
@Injectable()
export class AspectVisibilityPopupService {
    private isOpen = false;
    constructor(
        private modalService: NgbModal,
        private router: Router,
        private aspectVisibilityService: AspectVisibilityService

    ) {}

    open(component: Component, id?: number | any): NgbModalRef {
        if (this.isOpen) {
            return;
        }
        this.isOpen = true;

        if (id) {
            this.aspectVisibilityService.find(id).subscribe((aspectVisibility) => {
                if (aspectVisibility.createdAt) {
                    aspectVisibility.createdAt = {
                        year: aspectVisibility.createdAt.getFullYear(),
                        month: aspectVisibility.createdAt.getMonth() + 1,
                        day: aspectVisibility.createdAt.getDate()
                    };
                }
                if (aspectVisibility.updatedAt) {
                    aspectVisibility.updatedAt = {
                        year: aspectVisibility.updatedAt.getFullYear(),
                        month: aspectVisibility.updatedAt.getMonth() + 1,
                        day: aspectVisibility.updatedAt.getDate()
                    };
                }
                this.aspectVisibilityModalRef(component, aspectVisibility);
            });
        } else {
            return this.aspectVisibilityModalRef(component, new AspectVisibility());
        }
    }

    aspectVisibilityModalRef(component: Component, aspectVisibility: AspectVisibility): NgbModalRef {
        const modalRef = this.modalService.open(component, { size: 'lg', backdrop: 'static'});
        modalRef.componentInstance.aspectVisibility = aspectVisibility;
        modalRef.result.then((result) => {
            this.router.navigate([{ outlets: { popup: null }}], { replaceUrl: true });
            this.isOpen = false;
        }, (reason) => {
            this.router.navigate([{ outlets: { popup: null }}], { replaceUrl: true });
            this.isOpen = false;
        });
        return modalRef;
    }
}

import { Injectable, Component } from '@angular/core';
import { Router } from '@angular/router';
import { NgbModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { Profile } from './profile.model';
import { ProfileService } from './profile.service';
@Injectable()
export class ProfilePopupService {
    private isOpen = false;
    constructor(
        private modalService: NgbModal,
        private router: Router,
        private profileService: ProfileService

    ) {}

    open(component: Component, id?: number | any): NgbModalRef {
        if (this.isOpen) {
            return;
        }
        this.isOpen = true;

        if (id) {
            this.profileService.find(id).subscribe((profile) => {
                if (profile.birthday) {
                    profile.birthday = {
                        year: profile.birthday.getFullYear(),
                        month: profile.birthday.getMonth() + 1,
                        day: profile.birthday.getDate()
                    };
                }
                this.profileModalRef(component, profile);
            });
        } else {
            return this.profileModalRef(component, new Profile());
        }
    }

    profileModalRef(component: Component, profile: Profile): NgbModalRef {
        const modalRef = this.modalService.open(component, { size: 'lg', backdrop: 'static'});
        modalRef.componentInstance.profile = profile;
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

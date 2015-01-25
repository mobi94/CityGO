//
//  CGSignUpViewController.m
//  CityGo
//
//  Created by ruslan on 1/24/15.
//  Copyright (c) 2015 Ruslan Moskalenko. All rights reserved.
//

#import "CGSignUpViewController.h"

@interface CGSignUpViewController ()

@property (weak, nonatomic) IBOutlet UIButton *dismissButton;
@property (weak, nonatomic) IBOutlet UIButton *addAvatarButton;
@property (weak, nonatomic) IBOutlet UITextField *usernameTextField;
@property (weak, nonatomic) IBOutlet UITextField *passwordTextField;
@property (weak, nonatomic) IBOutlet UITextField *confirmPasswordTextField;
@property (weak, nonatomic) IBOutlet UITextField *emailTextField;
@property (weak, nonatomic) IBOutlet UITextField *birthdayTextField;
@property (weak, nonatomic) IBOutlet UIButton *signupButton;


- (IBAction)addAvatarButonClick:(id)sender;
- (IBAction)signupButtonClick:(id)sender;
@end

@implementation CGSignUpViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

/*
#pragma mark - Navigation

// In a storyboard-based application, you will often want to do a little preparation before navigation
- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender {
    // Get the new view controller using [segue destinationViewController].
    // Pass the selected object to the new view controller.
}
*/

- (IBAction)dismissButtonClick:(id)sender
{
    [self dismissViewControllerAnimated:YES completion:nil];
}

- (IBAction)addAvatarButonClick:(id)sender {
}

- (IBAction)signupButtonClick:(id)sender {
}
@end
